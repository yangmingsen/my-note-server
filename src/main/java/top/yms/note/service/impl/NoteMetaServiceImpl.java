package top.yms.note.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.*;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.conpont.task.AsyncTask;
import top.yms.note.conpont.task.DelayExecuteAsyncTask;
import top.yms.note.dao.NoteFileQuery;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.dto.NoteIndexLuceneUpdateDto;
import top.yms.note.dto.NoteMoveDto;
import top.yms.note.dto.NoteSearchCondition;
import top.yms.note.dto.NoteSearchDto;
import top.yms.note.entity.*;
import top.yms.note.enums.*;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.*;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.NoteIndexErrorCode;
import top.yms.note.service.NoteMetaService;
import top.yms.note.service.NoteUserService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.IdWorkerUtils;
import top.yms.note.utils.LocalThreadUtils;
import top.yms.note.vo.NoteInfoVo;
import top.yms.note.vo.NoteSearchVo;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@Service
public class NoteMetaServiceImpl implements NoteMetaService {
    private static final Logger log = LoggerFactory.getLogger(NoteMetaServiceImpl.class);

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Resource
    private NoteIndexUpdateLogMapper noteIndexLogMapper;

    @Resource
    private NoteFileMapper noteFileMapper;

    @Resource
    private NoteDataMapper noteDataMapper;

    @Resource
    private NoteDataVersionMapper noteDataVersionMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private IdWorker idWorker;

    @Qualifier(NoteConstants.noteLuceneSearch)
    @Resource
    private NoteSearchService noteSearchService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private NoteRecentVisitService noteRecentVisitService;

    @Resource
    private NoteAsyncExecuteTaskService noteAsyncExecuteTaskService;

    @Resource
    private NoteService noteService;

    @Resource
    private NoteRedisCacheService cacheService;

    @Resource
    private NoteUserService noteUserService;

    private final ReentrantLock LOCK = new ReentrantLock();

    private final Object OBJ = new Object();

    public List<NoteMeta> findByUserId(Long userid) {
        return Optional.of(findNoteList(userid, 1)).orElse(Collections.emptyList());
    }

    /**
     * 含过滤数据的查
     * @param uid
     * @param filter 1.查找所有(含文件和目录); 2.只查目录
     * @return
     */
    private List<NoteMeta> findNoteList(Long uid, int filter) {
        return noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).filter(filter).get().example());
    }


    /**
     * 根据siteId查询
     * @param siteId
     * @return
     */
    public NoteMeta findBySiteId(String siteId) {
        return noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().siteId(siteId).get().example()).get(0);
    }

    public List<NoteMeta> selectByParentId(Long parentId) {
        //find from cache
        Object cVal = cacheService.hGet(NoteCacheKey.NOTE_META_PARENT_LIST_KEY, parentId.toString());
        if (cVal != null) {
            return (List<NoteMeta>) cVal;
        }
        List<NoteMeta>  resList = noteMetaMapper.selectByParentId(parentId);
        cacheService.hSet(NoteCacheKey.NOTE_META_PARENT_LIST_KEY, parentId.toString(), resList);
        return resList;
    }

    /**
     * 查询noteIndex列表, 顺便修改访问时间
     * @param parentId
     * @param uid
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public List<NoteMeta> findNoteMetaList(Long parentId, Long uid) {
        //修改访问时间
        NoteMeta upNoteMeta = new NoteMeta();
        upNoteMeta.setId(parentId);
        upNoteMeta.setViewTime(new Date());
        update(upNoteMeta);
        //add cache search
//        String cKey = NoteCacheKey.NOTE_META_SUB_KEY+parentId+"_"+uid;
//        Object cVal = cacheService.get(cKey);
//        if (cVal != null) {
//            return (List<NoteMeta>) cVal;
//        }
//        List<NoteMeta> noteMetaList = noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().parentId(parentId).uid(uid).get().example());
        List<NoteMeta> noteMetaList = selectByParentId(parentId);
        //获取目录List
        List<Long> dirIds = noteMetaList.stream().filter(note -> NoteConstants.DIR_FLAG.equals(note.getIsFile())).map(NoteMeta::getId).collect(Collectors.toList());
        AsyncTask asyncTask = AsyncTask.Builder.build()
                .taskId(IdWorkerUtils.getId())
                .executeType(AsyncExcuteTypeEnum.SYNC_TASK)
                .type(AsyncTaskEnum.NOTE_DIR_SIZE_COMPUTE_TASK)
                .taskName(AsyncTaskEnum.NOTE_DIR_SIZE_COMPUTE_TASK.getName())
                .createTime(new Date())
                .userId(LocalThreadUtils.getUserId())
                .taskInfo(dirIds)
                .get();
        noteAsyncExecuteTaskService.addTask(asyncTask);
        //to cache.
//        cacheService.set(cKey, noteMetaList, 3 * 60L);
        //ret
        return noteMetaList;
    }

    /**
     * 查找某个用户的顶级目录
     * @return
     */
    public NoteMeta findRoot() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        return noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).parentId(0L).del(false).get().example()).get(0);
    }

    /**
     * 根据noteIndex id 回退到其父目录
     * @param id
     * @return
     */
    public List<NoteMeta> findBackParentDir(Long id) {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        NoteMeta note = findOne(id);
        return findNoteMetaList(note.getParentId(), uid);
    }

    /**
     * 根据id查找noteIndex 元数据
     * @param id
     * @return
     */
    public NoteMeta findOne(Long id) {
        Object cVal = cacheService.hGet(NoteCacheKey.NOTE_META_LIST_KEY, id.toString());
        if (cVal != null) {
            return (NoteMeta) cVal;
        }
        synchronized (OBJ) {
            cVal = cacheService.hGet(NoteCacheKey.NOTE_META_LIST_KEY, id.toString());
            if (cVal != null) {
                return (NoteMeta) cVal;
            }
            cVal = noteMetaMapper.selectByPrimaryKey(id);
            //to cache
            cacheService.hSet(NoteCacheKey.NOTE_META_LIST_KEY, id.toString(), cVal);
        }
        return (NoteMeta)cVal;
    }

    /**
     * 笔记全文搜索
     * 目前包含对标题和其内容
     * @param searchDto
     * @return
     */
    public NoteSearchVo findNoteByCondition(NoteSearchCondition searchDto) {
        NoteSearchDto noteSearchDto = new NoteSearchDto();
        noteSearchDto.setUserId(LocalThreadUtils.getUserId());
        noteSearchDto.setKeyword(searchDto.getSearchContent());
        return new NoteSearchVo(noteSearchService.doSearch(noteSearchDto));
    }

    /**
     * 查找当当前用户的笔记树.
     *  只查未删除掉的，但要注意只是未标记删除的
     * @return
     */
    public NoteTree findCurUserRootNoteTree() {
        Long userId = LocalThreadUtils.getUserId();
        List<NoteMeta> noteMetaList =  noteMetaMapper.selectByExample(
                NoteIndexQuery.Builder.build().uid(userId).del(false).get().example()
        );
        List<NoteTree> noteTreeList = transferNoteTree(noteMetaList);
        if (noteTreeList.size() > 0) {
            return noteTreeList.get(0);
        }
        return null;
    }

    private List<NoteTree> transferNoteTree(List<NoteMeta> noteMetaList) {
        //列表转换为结构树
        Map<Long, NoteTree> noteTreeMap = new HashMap<>();
        for (NoteMeta note : noteMetaList) {
            NoteTree tmpNoteTree = new NoteTree();
            tmpNoteTree.setId(note.getId());
            tmpNoteTree.setLabel(note.getName());
            tmpNoteTree.setParentId(note.getParentId());
            tmpNoteTree.setChildren(new LinkedList<>());
            noteTreeMap.put(tmpNoteTree.getId(), tmpNoteTree);
        }

        //各自找各自的父节点
        List<NoteTree> resList = new LinkedList<>();
        for(Map.Entry<Long, NoteTree> entry : noteTreeMap.entrySet()) {
            NoteTree value = entry.getValue();
            Long parentId = value.getParentId();
            NoteTree parentNoteTree = noteTreeMap.get(parentId);
            if (parentNoteTree != null) {
                parentNoteTree.getChildren().add(value);
            } else {
                //说明当前节点已经是顶层节点
                if (value.getParentId() == 0L)
                    resList.add(value);
            }
        }
        return resList;
    }

    /**
     * 查找目录树
     * 只有目录，不包含文件
     * @param uid
     * @return
     */
    public List<NoteTree> findNoteTreeByUid(Long uid) {
        List<NoteMeta> noteMetaList =  noteMetaMapper.selectByExample(
                NoteIndexQuery.Builder.build().uid(uid).del(false).filter(2).get().example()
        );
        //列表转换为结构树
        return transferNoteTree(noteMetaList);
    }

    /**
     * 为了支持阅读密码的目录树。
     * 当父目录是被要求密码访问的，那么其子目录不该出现的tree树节点中。
     *  因为防止直接绕过父目录访问到其子目录
     * @param userId
     * @return
     */
    public List<AntTreeNode> findAntTreeExcludeEncrypted(Long userId) {
        String cacheKey = NoteCacheKey.NOTE_META_TREE_KEY+userId;
        //add cache
        Object metaTreeCache = cacheService.get(cacheKey);
        if (metaTreeCache != null) {
            return (List<AntTreeNode>)metaTreeCache;
        }
        List<NoteMeta> noteMetaList =  noteMetaMapper.selectByExample(
                NoteIndexQuery.Builder.build().uid(userId).del(false).filter(2).get().example()
        );
        //列表转换为结构树
        Map<Long, NoteTree> noteTreeMap = new HashMap<>();
        Map<Long, NoteMeta> noteIndexMap = new HashMap<>();
        for (NoteMeta note : noteMetaList) {
            NoteTree tmpNoteTree = new NoteTree();
            tmpNoteTree.setId(note.getId());
            tmpNoteTree.setLabel(note.getName());
            tmpNoteTree.setParentId(note.getParentId());
            tmpNoteTree.setChildren(new LinkedList<>());
            noteTreeMap.put(tmpNoteTree.getId(), tmpNoteTree);
            noteIndexMap.put(note.getId(), note);
        }
        //各自找各自的父节点
        List<NoteTree> resList = new LinkedList<>();
        for(Map.Entry<Long, NoteTree> entry : noteTreeMap.entrySet()) {
            NoteTree value = entry.getValue();
            Long parentId = value.getParentId();
            NoteTree parentNoteTree = noteTreeMap.get(parentId);
            if (parentNoteTree != null) {
                parentNoteTree.getChildren().add(value);
            } else {
                //说明当前节点已经是顶层节点
                if (value.getParentId() == 0L)
                    resList.add(value);
            }
        }
        List<AntTreeNode> antTreeList = new LinkedList<>();
        for (NoteTree noteTree : resList) {
            antTreeList.add(transferToAntTree(noteTree, noteIndexMap));
        }
        //to cache
        cacheService.set(cacheKey, antTreeList, 10*60L);
        return antTreeList;
    }

    /**
     * 树转换及排序。 将NoteTree转换为antTree. 且进行子节点排序
     * @param noteTree
     * @param noteTreeMap
     * @return
     */
    private AntTreeNode transferToAntTree(NoteTree noteTree, final Map<Long, NoteMeta> noteTreeMap) {
        if (noteTree == null) return null;
        List<AntTreeNode> antTreeNodeList = new LinkedList<>();
        if (noteTree.getChildren() != null) {
            if (noteTreeMap.get(noteTree.getId()).getEncrypted().equals(NoteConstants.ENCRYPTED_UN_FLAG)) {
                for (NoteTree nTree : noteTree.getChildren()) {
                    antTreeNodeList.add(transferToAntTree(nTree, noteTreeMap));
                }
                //增加排序
                antTreeNodeList = antTreeNodeList.stream().sorted((o1, o2) -> {
                    NoteMeta note1 = noteTreeMap.get(Long.valueOf(o1.getKey()));
                    NoteMeta note2 = noteTreeMap.get(Long.valueOf(o2.getKey()));
                    //默认对访问时间的排序，规则为，如果访问时间不为空就就用访问时间, 如果空就用updateTime, 如果updateTime空，就用createTime
                    Date t1 = note1.getViewTime() != null ? note1.getViewTime() : note1.getUpdateTime() != null ? note1.getUpdateTime(): note1.getCreateTime();
                    Date t2 = note2.getViewTime() != null ? note2.getViewTime() : note2.getUpdateTime() != null ? note2.getUpdateTime(): note2.getCreateTime();
                    return t2.compareTo(t1);
                }).collect(Collectors.toList());
            }
        }
        return new AntTreeNode(noteTree.getLabel(), noteTree.getId().toString(), antTreeNodeList);
    }

    //处理加密的文件夹, 去除掉子目录

    /**
     * NoteTree 转 AntTree.
     * 由于之前使用的是element-ui的tree组件, 现在改为使用antd-ui的tree组件,
     *  需要转换一下名称
     * @param noteTree
     * @return
     */
    public AntTreeNode transferToAntTree(NoteTree noteTree) {
        if (noteTree == null) return null;
        List<AntTreeNode> antTreeNodeList = new LinkedList<>();
        if (noteTree.getChildren() != null) {
            for (NoteTree nTree : noteTree.getChildren()) {
                antTreeNodeList.add(transferToAntTree(nTree));
            }
        }
        return new AntTreeNode(noteTree.getLabel(), noteTree.getId().toString(), antTreeNodeList);
    }

    /**
     * 暂不使用, 因为没有加密阅读隐藏功能。 请使用 findAntTreeExcludeEncrypted
     * @param userId
     * @return
     */
    @Deprecated
    public List<AntTreeNode> findAntTreeNode(Long userId) {
        List<NoteTree> noteTreeList = findNoteTreeByUid(userId);
        List<AntTreeNode> antTreeList = new LinkedList<>();
        for (NoteTree noteTree : noteTreeList) {
            antTreeList.add(transferToAntTree(noteTree));
        }
        return antTreeList;
    }

    /**
     * 新增noteIndex笔记元数据
     * @param note
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void add(NoteMeta note) {
        String mindMapMongoId = null;
        try {
            //父目录检查，考虑取消。 因为多文件上传可能次序不一致。 考虑用定时任务去检查无父目录的情况 2025-06-25
//            List<NoteMeta> noteMetaList = noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().nid(note.getParentId()).get().example());
//            if (noteMetaList.size() == 0) {
//                throw new BusinessException(NoteIndexErrorCode.E_203110);
//            }
            Date opTime;
            if (note.getCreateTime() != null) {
                opTime = note.getCreateTime();
            } else {
                opTime = new Date();
            }
            long genId;
            if (note.getId() == null) {
                genId = idWorker.nextId();
                note.setId(genId);
            } else {
                genId = note.getId();
            }
            String noteType = note.getType();
            if (FileTypeEnum.WER.compare(noteType) || FileTypeEnum.MARKDOWN.compare(noteType)) {
                note.setStoreSite(NoteConstants.MYSQL);
                note.setSiteId("");
            } else if(FileTypeEnum.MINDMAP.compare(noteType)) {
                note.setStoreSite(NoteConstants.MONGO);
                //create default value
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", genId+"");
                JSONArray jsonArray = new JSONArray();
                JSONObject arJson = new JSONObject();
                arJson.put("name", "新建标题");
                jsonArray.add(arJson);
                jsonObject.put("content", jsonArray);
                log.debug("defaultJsonStr: {}", jsonObject);
                Document document = Document.parse(jsonObject.toString());
                Document saveRes = mongoTemplate.save(document, NoteConstants.noteMindMap);
                ObjectId objId = saveRes.getObjectId("_id");
                note.setSiteId(objId.toString());
                mindMapMongoId = objId.toString();
            }
            Long parentId = note.getParentId();
            if (parentId == null) {
                note.setParentId(0L);
            }
            note.setCreateTime(opTime);
            noteMetaMapper.insertSelective(note);
            //update cache
            cacheService.hDel(NoteCacheKey.NOTE_META_PARENT_LIST_KEY, note.getParentId().toString());
            //insert log
            NoteIndexUpdateLog logData = new NoteIndexUpdateLog();
            logData.setIndexId(genId);
            logData.setCreateTime(opTime);
            logData.setType(NoteOpTypeEnum.ADD);
            Gson gson = new Gson();
            String gsonStr = gson.toJson(note);
            logData.setContent(gsonStr);
            noteIndexLogMapper.insert(logData);
            //update index service
            NoteLuceneIndex noteLuceneIndex = new NoteLuceneIndex();
            noteLuceneIndex.setId(genId);
            noteLuceneIndex.setUserId(LocalThreadUtils.getUserId());
            noteLuceneIndex.setParentId(note.getParentId());
            noteLuceneIndex.setTitle(note.getName());
            noteLuceneIndex.setIsFile(note.getIsFile());
            noteLuceneIndex.setType(note.getType());
            noteLuceneIndex.setCreateDate(opTime);
            noteLuceneIndex.setEncrypted("0");
            noteAsyncExecuteTaskService.addTask(
                    DelayExecuteAsyncTask.Builder
                            .build()
                            .type(AsyncTaskEnum.SYNC_Note_Index_UPDATE)
                            .executeType(AsyncExcuteTypeEnum.DELAY_EXC_TASK)
                            .taskId(idWorker.nextId())
                            .taskName(AsyncTaskEnum.SYNC_Note_Index_UPDATE.getName())
                            .createTime(new Date())
                            .userId(LocalThreadUtils.getUserId())
                            .taskInfo(NoteIndexLuceneUpdateDto.Builder.build().type(NoteIndexLuceneUpdateDto.updateNoteIndex).data(noteLuceneIndex).get())
                            .get()
            );
        } catch (Exception e) {
            log.error("add失败", e);
            if (mindMapMongoId != null) {
                mongoTemplate.remove(new Document("_id", mindMapMongoId), NoteConstants.noteMindMap);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * noteIndex笔记更新
     * @param note
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 60)
    public void update(NoteMeta note) {
        Long id = note.getId();
        NoteMeta noteMeta = findOne(id);
        if (noteMeta == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203106);
        }
        if (StringUtils.isNotBlank(note.getName())) {
            if (!note.getName().equals(noteMeta.getName())) {
                //update index service
                NoteLuceneIndex noteLuceneIndex = new NoteLuceneIndex();
                noteLuceneIndex.setId(id);
                noteLuceneIndex.setUserId(LocalThreadUtils.getUserId());
                noteLuceneIndex.setParentId(note.getParentId());
                noteLuceneIndex.setTitle(note.getName());
                noteLuceneIndex.setIsFile(note.getIsFile());
                noteLuceneIndex.setType(note.getType());
                noteLuceneIndex.setCreateDate(new Date());
                noteLuceneIndex.setEncrypted("0");
                DelayExecuteAsyncTask indexUpdateDelayTask = DelayExecuteAsyncTask.Builder
                        .build()
                        .type(AsyncTaskEnum.SYNC_Note_Index_UPDATE)
                        .executeType(AsyncExcuteTypeEnum.DELAY_EXC_TASK)
                        .taskId(idWorker.nextId())
                        .taskName(AsyncTaskEnum.SYNC_Note_Index_UPDATE.getName())
                        .createTime(new Date())
                        .userId(LocalThreadUtils.getUserId())
                        .taskInfo(NoteIndexLuceneUpdateDto.Builder.build().type(NoteIndexLuceneUpdateDto.updateNoteIndex).data(noteLuceneIndex).get())
                        .get();
                noteAsyncExecuteTaskService.addTask(indexUpdateDelayTask);
            }
        }
        //update cache
        //1.del cache
        cacheService.hDel(NoteCacheKey.NOTE_META_LIST_KEY, noteMeta.getId().toString());
        //1.1 del sub tree
        cacheService.hDel(NoteCacheKey.NOTE_META_PARENT_LIST_KEY, noteMeta.getParentId().toString());
        //1.2 del ant tree
        String cKey =  NoteCacheKey.NOTE_META_TREE_KEY+noteMeta.getUserId();
        cacheService.del(cKey);
        //update meta data
        note.setUpdateTime(new Date());
        noteMetaMapper.updateByPrimaryKeySelective(note);
        //udpate log insert
        NoteIndexUpdateLog logData = new NoteIndexUpdateLog();
        logData.setIndexId(note.getId());
        logData.setCreateTime(new Date());
        logData.setType(NoteOpTypeEnum.UPDATE);
        Gson gson = new Gson();
        String gsonStr = gson.toJson(note);
        logData.setContent(gsonStr);
        noteIndexLogMapper.insert(logData);
    }

    /**
     * 物理删除笔记文件或文件夹
     * 这里的删除不仅会物理删除noteIndex, 还会物理删除掉跟当前笔记相关的资源（如存储在mongodb上的图片,文件等)
     * @param id
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 10)
    public void destroyNote(Long id) {
        NoteMeta noteMeta = findOne(id);
        if (noteMeta == null) {
            log.error("destroyNote id={}, 未找到索引信息", id);
            return ;
        }
        if (noteMeta.getParentId() == 0L) {
            throw new BusinessException(NoteIndexErrorCode.E_203114);
        }
        if (NoteTypeEnum.File == NoteTypeEnum.apply(noteMeta.getIsFile())) {
            //删除t_note_index
            noteMetaMapper.deleteByPrimaryKey(id);
            //删除t_note_data
            if (NoteConstants.MYSQL.equals(noteMeta.getStoreSite())) {
                noteDataMapper.deleteByPrimaryKey(id);
                noteDataVersionMapper.deleteByNoteId(id);
                noteFileMapper.selectByNoteRef(id)
                        .forEach(noteFile -> fileStoreService.delFile(noteFile.getFileId()));
                noteFileMapper.deleteByNoteRef(id);
            }
            //删除t_note_file
            if (NoteConstants.MONGO.equals(noteMeta.getStoreSite())) {
                String siteId = noteMeta.getSiteId();
                noteFileMapper.deleteByFileId(siteId);
                fileStoreService.delFile(siteId);
            }
            NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
            addLog.setIndexId(id);
            addLog.setType(NoteOpTypeEnum.Destroy);
            addLog.setCreateTime(new Date());
            noteIndexLogMapper.insert(addLog);
            //删除索引
//            noteDataIndexService.delete(id);
            DelayExecuteAsyncTask indexUpdateDelayTask = DelayExecuteAsyncTask.Builder
                    .build()
                    .type(AsyncTaskEnum.SYNC_Note_Index_UPDATE)
                    .executeType(AsyncExcuteTypeEnum.DELAY_EXC_TASK)
                    .taskId(idWorker.nextId())
                    .taskName(AsyncTaskEnum.SYNC_Note_Index_UPDATE.getName())
                    .createTime(new Date())
                    .userId(LocalThreadUtils.getUserId())
                    .taskInfo(NoteIndexLuceneUpdateDto.Builder.build().type(NoteIndexLuceneUpdateDto.deleteOne).data(id).get())
                    .get();
            noteAsyncExecuteTaskService.addTask(indexUpdateDelayTask);
        }
        if (NoteTypeEnum.Directory == NoteTypeEnum.apply(noteMeta.getIsFile())) {
            final List<NoteMeta> noteMetaList = bfsSearchTree(id);
            final List<NoteIndexUpdateLog> addLogList = new LinkedList<>();
            final List<Long> delIdxList = new LinkedList<>();
            for (NoteMeta note : noteMetaList) {
                noteMetaMapper.deleteByPrimaryKey(note.getId());
                if (NoteTypeEnum.File == NoteTypeEnum.apply(note.getIsFile())) {
                    delIdxList.add(note.getId());
                    //删除t_note_data
                    if (NoteConstants.MYSQL.equals(note.getStoreSite())) {
                        noteDataMapper.deleteByPrimaryKey(note.getId());
                        noteDataVersionMapper.deleteByNoteId(note.getId());
                        noteFileMapper.selectByNoteRef(note.getId())
                                .forEach(noteFile -> fileStoreService.delFile(noteFile.getFileId()));
                        noteFileMapper.deleteByNoteRef(note.getId());
                    }
                    //删除t_note_file
                    if (NoteConstants.MONGO.equals(note.getStoreSite())) {
                        String siteId = note.getSiteId();
                        noteFileMapper.deleteByFileId(siteId);
                        fileStoreService.delFile(siteId);
                    }
                }
                //日志
                NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
                addLog.setIndexId(note.getId());
                addLog.setType(NoteOpTypeEnum.Destroy);
                addLog.setCreateTime(new Date());
                addLogList.add(addLog);
            }
            noteIndexLogMapper.insertBatch(addLogList);
            //删除索引
//            noteDataIndexService.delete(fileIds);
            DelayExecuteAsyncTask indexUpdateDelayTask = DelayExecuteAsyncTask.Builder
                    .build()
                    .type(AsyncTaskEnum.SYNC_Note_Index_UPDATE)
                    .executeType(AsyncExcuteTypeEnum.DELAY_EXC_TASK)
                    .taskId(idWorker.nextId())
                    .taskName(AsyncTaskEnum.SYNC_Note_Index_UPDATE.getName())
                    .createTime(new Date())
                    .userId(LocalThreadUtils.getUserId())
                    .taskInfo(NoteIndexLuceneUpdateDto.Builder.build().type(NoteIndexLuceneUpdateDto.deleteList).data(delIdxList).get())
                    .get();
            noteAsyncExecuteTaskService.addTask(indexUpdateDelayTask);
            log.debug("删除目录[{}]成功, 共[{}]条数据", noteMeta.getName(), addLogList.size());
        }
    }

    /**
     * 标记删除noteIndex
     * @param id
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 10)
    public void delNote(Long id) {
        NoteMeta noteMeta = findOne(id);
        if (noteMeta == null) return;
        if (noteMeta.getParentId() == 0L) {
            throw new BusinessException(NoteIndexErrorCode.E_203114);
        }
        NoteMeta up = new NoteMeta();
        up.setId(id);
        up.setDel("1");
        up.setUpdateTime(new Date());
        NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
        addLog.setIndexId(id);
        addLog.setType(NoteOpTypeEnum.DELETE);
        addLog.setCreateTime(new Date());
        update(up);
        //update cache
        cacheService.hDel(NoteCacheKey.NOTE_META_PARENT_LIST_KEY, noteMeta.getParentId().toString());
        //insert log
        noteIndexLogMapper.insert(addLog);
    }

    /**
     * bfs搜索某棵树节点
     * @param parentId
     * @return
     */
    private List<NoteMeta> bfsSearchTree(Long parentId) {
        Queue<Long> queue = new LinkedList<>();
        List<NoteMeta> resList = new LinkedList<>();
        resList.add(findOne(parentId));
        queue.add(parentId);
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            List<NoteMeta> noteList = noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().parentId(id).get().example());
            if (noteList.size() > 0) {
                queue.addAll(noteList.stream().map(NoteMeta::getId).collect(Collectors.toList()));
                resList.addAll(noteList);
            }
        }
        return resList;
    }

    /**
     * 执行删除标记
     * @param parentId
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void delDir(Long parentId) {
        Queue<Long> queue = new LinkedList<>();
        List<Long> delList = new LinkedList<>();
        List<NoteIndexUpdateLog> addList = new LinkedList<>();
        queue.add(parentId);
        //BFS Search delete
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            List<NoteMeta> noteList = noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().parentId(id).get().example());
            if (noteList.size() > 0) {
                queue.addAll(noteList.stream().map(NoteMeta::getId).collect(Collectors.toList()));
            }
            NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
            addLog.setIndexId(id);
            addLog.setType(NoteOpTypeEnum.DELETE);
            addLog.setCreateTime(new Date());
            addList.add(addLog);
            delList.add(id);
        }
        noteMetaMapper.delByListIds(delList);
        //update cache
        NoteMeta noteMeta = findOne(parentId); //查询当前删除母元数据
        //更新父目录缓存
        cacheService.hDel(NoteCacheKey.NOTE_META_PARENT_LIST_KEY, noteMeta.getParentId().toString());
        noteIndexLogMapper.insertBatch(addList);
        log.debug("delDir: 删除成功, 共计 {} 条数据", delList.size());
    }

    public List<NoteMeta> findBy(NoteIndexQuery query) {
        return Optional.ofNullable(noteMetaMapper.selectByExample(query.example())).orElse(Collections.emptyList());
    }

    /**
     * 获取面包线：
     *  结果： root-> sub1 -> sub2
     * @param id
     * @param list
     */
    public void findBreadcrumb(Long id, List<NoteMeta> list) {
        if (id == 0L) {
            return;
        }
        NoteMeta noteMeta = findOne(id);
        if (noteMeta == null) return; //bug202506180932 发现若是存在错误数据则会NPE
        Long parentId = noteMeta.getParentId();
        if (parentId == 0) {
            list.add(noteMeta);
            return ;
        }
        findBreadcrumb(parentId, list);
        list.add(noteMeta);
    }


    /**
     * 给全文索引提供面包线数据
     * @param noteId 用parentId
     * @return
     */
    public String findBreadcrumbForSearch(Long noteId) {
        List<NoteMeta> resList = new LinkedList<>();
        findBreadcrumb(noteId, resList);
        final StringBuilder tmpPath = new StringBuilder();
        for(int i=0; i<resList.size(); i++) {
            if (i+1 == resList.size()) {
                tmpPath.append(resList.get(i).getName());
            } else {
                tmpPath.append(resList.get(i).getName()).append("/");
            }
        }
        return tmpPath.toString();
    }


    /**
     * 获取一个笔记的信息及存储位置信息
     * @param id
     * @return
     */
    public NoteInfoVo getNoteAndSite(Long id) {
        NoteMeta noteMeta = findOne(id);
        NoteInfoVo res = new NoteInfoVo();
        res.setNoteIndex(noteMeta);
        if (StringUtils.isNotBlank(noteMeta.getStoreSite())) {
            if (NoteConstants.MONGO.equals(noteMeta.getStoreSite())) {
                String siteId = noteMeta.getSiteId();
                List<NoteFile> noteFiles = noteFileMapper.selectByExample(NoteFileQuery.Builder.build().fileId(siteId).get().example());
                if (noteFiles != null && noteFiles.size() >0) {
                    res.setNoteFile(noteFiles.get(0));
                }
            }
        }
        return res;
    }

    /**
     * 获取最近新添加或者修改过的文件列表
     * @return
     */
    public List<NoteMeta> getRecentFiles() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        return noteMetaMapper.selectRecentUpdate(uid).stream()
                .limit(30).collect(Collectors.toList());
    }

    /**
     * 获取被标记为删除的文件列表
     * @return
     */
    public List<NoteMeta> getDeletedFiles() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        return noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).del(true).get().example());
    }

    /**
     * 物理删除掉，被标记为删除的笔记文件/文件夹。 注意该方法的结果不可逆
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 15)
    public int allDestroy() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        List<NoteMeta> destroyNoteList = noteMetaMapper.selectDestroyNotes(uid);
        for(NoteMeta destroyNote : destroyNoteList) {
            destroyNote(destroyNote.getId());
        }
        return destroyNoteList.size();
    }

    /**
     * 恢复所有被标记为删除的笔记文件/文件夹, 变为原来的样子
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public int allRecover() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        return noteMetaMapper.allRecover(uid);
    }

    /**
     * 操作文件到文件夹中的移动
     * @param noteMoveDto
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void updateMove(NoteMoveDto noteMoveDto) {
        Long toId = noteMoveDto.getToId();
        NoteMeta noteMeta = findOne(toId);
        if (!NoteConstants.DIR_FLAG.equals(noteMeta.getIsFile())) {
            throw new BusinessException(BusinessErrorCode.E_204011);
        }
        NoteMeta upDo = new NoteMeta();
        upDo.setId(noteMoveDto.getFromId());
        upDo.setParentId(noteMoveDto.getToId());
        update(upDo);
        //update cache
        cacheService.hDel(NoteCacheKey.NOTE_META_PARENT_LIST_KEY, noteMeta.getParentId().toString(), noteMoveDto.getToId().toString());
    }

    /**
     * 加密笔记文件/文件夹
     *  意思是，让访问笔记文件和文件夹需要输入相关密码
     * @param id
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void encryptedReadNote(Long id) {
        NoteMeta upDao = new NoteMeta();
        upDao.setEncrypted("1");
        upDao.setId(id);
        update(upDao);
        //加密内容
        noteService.encryptNote(id);
    }

    /**
     * 设置为未加密
     * @param id
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void unEncryptedReadNote(Long id) {
        NoteMeta upDao = new NoteMeta();
        upDao.setEncrypted("0");
        upDao.setId(id);
        update(upDao);
        //解密
        noteService.decryptNote(id);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void autoScanEncrypt() {
        Long userId = LocalThreadUtils.getUserId();
        NoteMeta qry = new NoteMeta();
        qry.setUserId(userId);
        qry.setEncrypted(NoteConstants.ENCRYPTED_FLAG);
        List<NoteMeta> encryptedList = noteMetaMapper.selectByCondition(qry);
        for (NoteMeta noteMeta : encryptedList) {
            noteService.encryptNote(noteMeta.getId());
        }
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void autoDecryptedAllNote() {
        Long userId = LocalThreadUtils.getUserId();
        NoteMeta qry = new NoteMeta();
        qry.setUserId(userId);
        qry.setEncrypted(NoteConstants.ENCRYPTED_FLAG);
        List<NoteMeta> encryptedList = noteMetaMapper.selectByCondition(qry);
        for (NoteMeta noteMeta : encryptedList) {
            noteService.decryptNote(noteMeta.getId());
            //更新为未加密
            noteMeta.setEncrypted(NoteConstants.ENCRYPTED_UN_FLAG);
            update(noteMeta);
        }
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    @Override
    public NoteMeta createDir(String dirName, Long parentId) {
        long id = idWorker.nextId();
        NoteMeta noteMeta = new NoteMeta();
        noteMeta.setId(id);
        noteMeta.setParentId(parentId);
        noteMeta.setName(dirName);
        noteMeta.setUserId(noteUserService.getUserId());
        noteMeta.setIsFile(NoteConstants.DIR_FLAG);
        Date date = new Date();
        noteMeta.setCreateTime(date);
        noteMeta.setUpdateTime(date);
        noteMetaMapper.insertSelective(noteMeta);
        //ret
        noteMeta = findOne(id);
        return noteMeta;
    }

    /**
     * 获取最近访问列表
     * @return
     */
    public List<NoteMeta> findRecentVisitList() {
        return noteRecentVisitService.getRecentVisitList();
    }

    @Override
    public List<NoteMeta> findNoteMetaByParentId(Long parentId) {
        return bfsSearchTree(parentId);
    }
}
