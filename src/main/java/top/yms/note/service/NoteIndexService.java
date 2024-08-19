package top.yms.note.service;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.conpont.FileStore;
import top.yms.note.dao.NoteFileQuery;
import top.yms.note.dto.NoteSearchCondition;
import top.yms.note.entity.*;
import top.yms.note.enums.NoteTypeEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.comm.Constants;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.enums.NoteOpTypeEnum;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.mapper.*;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;
import top.yms.note.vo.NoteIndexSearchResult;
import top.yms.note.vo.NoteInfoVo;
import top.yms.note.vo.NoteSearchVo;
import top.yms.note.vo.SearchResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@Service
public class NoteIndexService {

    private static final Logger log = LoggerFactory.getLogger(NoteIndexService.class);

    @Autowired
    private NoteIndexMapper noteIndexMapper;

    @Autowired
    private NoteIndexUpdateLogMapper noteIndexLogMapper;

    @Autowired
    private NoteFileMapper noteFileMapper;

    @Autowired
    private NoteDataMapper noteDataMapper;

    @Autowired
    private NoteDataVersionMapper noteDataVersionMapper;

    @Autowired
    private FileStore fileStoreService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private NoteSearchLogService noteSearchLogService;

    public List<NoteIndex> findByUserId(Long userid) {
        return Optional.of(findNoteList(userid, 1)).orElse(Collections.emptyList());
    }

    /**
     * 含过滤数据的查
     * @param uid
     * @param filter 1.查找所有(含文件和目录); 2.只查目录
     * @return
     */
    private List<NoteIndex> findNoteList(Long uid, int filter) {
        return noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).filter(filter).get().example());
    }

    public List<NoteIndex> findSubBy(Long parentId, Long uid) {
        return noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().parentId(parentId).uid(uid).get().example());
    }

    public NoteIndex findRoot() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        return noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).parentId(0L).del(false).get().example()).get(0);
    }

    public List<NoteIndex> findBackParentDir(Long id) {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        NoteIndex note = noteIndexMapper.selectByPrimaryKey(id);

        return findSubBy(note.getParentId(), uid);
    }

    public NoteIndex findOne(Long id) {
        return noteIndexMapper.selectByPrimaryKey(id);
    }

    public NoteSearchVo findNoteByCondition(NoteSearchCondition searchDto) {

        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        //add search log
        SearchLog searchLog = new SearchLog();
        searchLog.setId(idWorker.nextId());
        searchLog.setSearchContent(searchDto.getSearchContent());
        searchLog.setCreateTime(new Date());
        searchLog.setUserId(uid);
        noteSearchLogService.add(searchLog);


        List<SearchResult> searchResults = noteIndexMapper.searchName(searchDto.getSearchContent(), uid)
                .stream()
                .map(noteIndex -> {
                    NoteIndexSearchResult searchResult = new NoteIndexSearchResult();
                    searchResult.setResult(noteIndex.getName());
                    searchResult.setId(noteIndex.getId());
                    searchResult.setParentId(noteIndex.getParentId());
                    searchResult.setIsile(noteIndex.getIsile());
                    searchResult.setType(noteIndex.getType());
                    return (SearchResult)searchResult;
                })
                .collect(Collectors.toList());


        return new NoteSearchVo(searchResults);
    }

    /**
     * 查找目录树
     * @param uid
     * @return
     */
    public List<NoteTree> findNoteTreeByUid(Long uid) {
        List<NoteIndex> noteIndexList =  noteIndexMapper.selectByExample(
                NoteIndexQuery.Builder.build().uid(uid).del(false).filter(2).get().example()
        );
        //列表转换为结构树
        Map<Long, NoteTree> noteTreeMap = new HashMap<>();
        for (NoteIndex note : noteIndexList) {
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

    public List<AntTreeNode> findAntTreeNode(Long userId) {
        List<NoteTree> noteTreeList = findNoteTreeByUid(userId);
        List<AntTreeNode> antTreeList = new LinkedList<>();
        for (NoteTree noteTree : noteTreeList) {
            antTreeList.add(transferToAntTree(noteTree));
        }

        return antTreeList;
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void add(NoteIndex note) {
        List<NoteIndex> noteIndexList = noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().nid(note.getParentId()).get().example());
        if (noteIndexList.size() == 0) {
            throw new BusinessException(NoteIndexErrorCode.E_203110);
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
            note.setStoreSite(Constants.MYSQL);
            note.setSiteId("");
        }

        Long parentId = note.getParentId();
        if (parentId == null) {
            note.setParentId(0L);
        }
        note.setCreateTime(new Date());
        noteIndexMapper.insertSelective(note);

        NoteIndexUpdateLog logData = new NoteIndexUpdateLog();
        logData.setIndexId(genId);
        logData.setCreateTime(new Date());
        logData.setType(NoteOpTypeEnum.ADD);
        Gson gson = new Gson();
        String gsonStr = gson.toJson(note);
        logData.setContent(gsonStr);
        noteIndexLogMapper.insert(logData);

    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void update(NoteIndex node) {
        Long id = node.getId();
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        if (noteIndex == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203106);
        }

        node.setUpdateTime(new Date());
        noteIndexMapper.updateByPrimaryKeySelective(node);

        NoteIndexUpdateLog logData = new NoteIndexUpdateLog();
        logData.setIndexId(node.getId());
        logData.setCreateTime(new Date());
        logData.setType(NoteOpTypeEnum.UPDATE);
        Gson gson = new Gson();
        String gsonStr = gson.toJson(node);
        logData.setContent(gsonStr);
        noteIndexLogMapper.insert(logData);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 10)
    public void destroyNote(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        if (noteIndex == null) {
            log.error("destroyNote id={}, 未找到索引信息", id);
            return ;
        }
        if (noteIndex.getParentId() == 0L) {
            throw new BusinessException(NoteIndexErrorCode.E_203114);
        }

        if (NoteTypeEnum.File == NoteTypeEnum.apply(noteIndex.getIsile())) {
            //删除t_note_index
            noteIndexMapper.deleteByPrimaryKey(id);
            //删除t_note_data
            if (Constants.MYSQL.equals(noteIndex.getStoreSite())) {
                noteDataMapper.deleteByPrimaryKey(id);
                noteDataVersionMapper.deleteByNoteId(id);
                noteFileMapper.selectByNoteRef(id)
                        .forEach(noteFile -> fileStoreService.delFile(noteFile.getFileId()));
                noteFileMapper.deleteByNoteRef(id);
            }
            //删除t_note_file
            if (Constants.MONGO.equals(noteIndex.getStoreSite())) {
                String siteId = noteIndex.getSiteId();
                noteFileMapper.deleteByFileId(siteId);
                fileStoreService.delFile(siteId);
            }

            NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
            addLog.setIndexId(id);
            addLog.setType(NoteOpTypeEnum.Destroy);
            addLog.setCreateTime(new Date());
            noteIndexLogMapper.insert(addLog);
        }

        if (NoteTypeEnum.Directory == NoteTypeEnum.apply(noteIndex.getIsile())) {
            List<NoteIndex> noteIndexList = bfsSearchTree(id);
            List<NoteIndexUpdateLog> addLogList = new LinkedList<>();
            for (NoteIndex note : noteIndexList) {
                noteIndexMapper.deleteByPrimaryKey(note.getId());
                if (NoteTypeEnum.File == NoteTypeEnum.apply(note.getIsile())) {
                    //删除t_note_data
                    if (Constants.MYSQL.equals(note.getStoreSite())) {
                        noteDataMapper.deleteByPrimaryKey(note.getId());
                        noteDataVersionMapper.deleteByNoteId(note.getId());
                        noteFileMapper.selectByNoteRef(note.getId())
                                .forEach(noteFile -> fileStoreService.delFile(noteFile.getFileId()));
                        noteFileMapper.deleteByNoteRef(note.getId());
                    }
                    //删除t_note_file
                    if (Constants.MONGO.equals(note.getStoreSite())) {
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
            log.info("删除目录[{}]成功, 共[{}]条数据", noteIndex.getName(), addLogList.size());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 10)
    public void delNote(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        if (noteIndex == null) return;
        if (noteIndex.getParentId() == 0L) {
            throw new BusinessException(NoteIndexErrorCode.E_203114);
        }
        NoteIndex up = new NoteIndex();
        up.setId(id);
        up.setDel("1");
        up.setUpdateTime(new Date());

        NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
        addLog.setIndexId(id);
        addLog.setType(NoteOpTypeEnum.DELETE);
        addLog.setCreateTime(new Date());

        noteIndexMapper.updateByPrimaryKeySelective(up);
        noteIndexLogMapper.insert(addLog);
    }

    public List<NoteIndex> bfsSearchTree(Long parentId) {
        Queue<Long> queue = new LinkedList<>();
        List<NoteIndex> resList = new LinkedList<>();
        resList.add(noteIndexMapper.selectByPrimaryKey(parentId));
        queue.add(parentId);
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            List<NoteIndex> noteList = noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().parentId(id).get().example());
            if (noteList.size() > 0) {
                queue.addAll(noteList.stream().map(NoteIndex::getId).collect(Collectors.toList()));
                resList.addAll(noteList);
            }
        }

        return resList;
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void delDir(Long parentId) {
        Queue<Long> queue = new LinkedList<>();
        List<Long> delList = new LinkedList<>();
        List<NoteIndexUpdateLog> addList = new LinkedList<>();
        queue.add(parentId);

        //BFS Search delete
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            List<NoteIndex> noteList = noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().parentId(id).get().example());
            if (noteList.size() > 0) {
                queue.addAll(noteList.stream().map(NoteIndex::getId).collect(Collectors.toList()));
            }

            NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
            addLog.setIndexId(id);
            addLog.setType(NoteOpTypeEnum.DELETE);
            addLog.setCreateTime(new Date());

            addList.add(addLog);
            delList.add(id);
        }
        noteIndexMapper.delByListIds(delList);
        noteIndexLogMapper.insertBatch(addList);
        log.info("delDir: 删除成功, 共计 {} 条数据", delList.size());

    }

    public List<NoteIndex> findBy(NoteIndexQuery query) {
        return Optional.ofNullable(noteIndexMapper.selectByExample(query.example())).orElse(Collections.emptyList());
    }

    public void findBreadcrumb(Long id, List<NoteIndex> list) {
        if (id == 0L) {
            return;
        }
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        Long parentId = noteIndex.getParentId();
        if (parentId == 0) {
            list.add(noteIndex);
            return ;
        }
        findBreadcrumb(parentId, list);
        list.add(noteIndex);
    }

    public NoteInfoVo getNoteAndSite(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        NoteInfoVo res = new NoteInfoVo();
        res.setNoteIndex(noteIndex);
        if (StringUtils.isNotBlank(noteIndex.getStoreSite())) {
            if (Constants.MONGO.equals(noteIndex.getStoreSite())) {
                String siteId = noteIndex.getSiteId();
                List<NoteFile> noteFiles = noteFileMapper.selectByExample(NoteFileQuery.Builder.build().fileId(siteId).get().example());
                if (noteFiles != null && noteFiles.size() >0) {
                    res.setNoteFile(noteFiles.get(0));
                }
            }
        }
        return res;
    }

    public List<NoteIndex> getRecentFiles() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        return noteIndexMapper.selectRecentUpdate(uid).stream()
                .sorted(
                (o1, o2) -> {
                    Date d1 = o1.getUpdateTime();
                    if (d1 == null) {
                        d1 = o1.getCreateTime();
                    }
                    Date d2 = o2.getUpdateTime();
                    if (d2 == null) {
                        d2 = o2.getCreateTime();
                    }
                    return d2.compareTo(d1);
                }
        ).limit(30).collect(Collectors.toList());
    }

    public List<NoteIndex> getDeletedFiles() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        return noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).del(true).get().example()).stream().sorted(
                (o1, o2) -> {
                    Date d1 = o1.getUpdateTime();
                    if (d1 == null) {
                        d1 = o1.getCreateTime();
                    }
                    Date d2 = o2.getUpdateTime();
                    if (d2 == null) {
                        d2 = o2.getCreateTime();
                    }
                    return d2.compareTo(d1);
                }
        ).collect(Collectors.toList());
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 15)
    public int allDestroy() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        List<NoteIndex> destroyNoteList = noteIndexMapper.selectDestroyNotes(uid);
        for(NoteIndex destroyNote : destroyNoteList) {
            destroyNote(destroyNote.getId());
        }
        return destroyNoteList.size();
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public int allRecover() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        return noteIndexMapper.allRecover(uid);
    }
}
