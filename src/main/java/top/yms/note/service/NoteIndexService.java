package top.yms.note.service;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.exception.BusinessException;
import top.yms.note.comm.Constants;
import top.yms.note.comm.FileTypeEnum;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.config.NoteOpType;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteIndexUpdateLog;
import top.yms.note.entity.NoteTree;
import top.yms.note.fun.FunTest;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.mapper.NoteIndexUpdateLogMapper;
import top.yms.note.utils.IdWorker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@Service
public class NoteIndexService {

    private Logger log = LoggerFactory.getLogger(NoteIndexService.class);

    @Autowired
    private NoteIndexMapper noteIndexMapper;


    @Autowired
    private NoteIndexUpdateLogMapper noteIndexLogMapper;

    @Autowired
    private IdWorker idWorker;



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

        List<NoteTree> resList = new LinkedList<>();
        for(Map.Entry<Long, NoteTree> entry : noteTreeMap.entrySet()) {
            NoteTree value = entry.getValue();
            Long parentId = value.getParentId();

            NoteTree parentNoteTree = noteTreeMap.get(parentId);
            if (parentNoteTree != null) {
                parentNoteTree.getChildren().add(value);
            } else {
                resList.add(value);
            }
        }

        return resList;
    }



    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
    public void add(NoteIndex note) {
        List<NoteIndex> noteIndexList = noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().nid(note.getParentId()).get().example());
        if (noteIndexList.size() == 0) {
            throw new BusinessException(NoteIndexErrorCode.E_203110);
        }

        long genId = idWorker.nextId();
        note.setId(genId);

        String noteType = note.getType();
        if (FileTypeEnum.WER.compare(noteType)) {
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
        logData.setType(NoteOpType.ADD);
        Gson gson = new Gson();
        String gsonStr = gson.toJson(note);
        logData.setContent(gsonStr);
        noteIndexLogMapper.insert(logData);

    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
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
        logData.setType(NoteOpType.UPDATE);
        Gson gson = new Gson();
        String gsonStr = gson.toJson(node);
        logData.setContent(gsonStr);
        noteIndexLogMapper.insert(logData);
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
    public void delDir(Long parentId) {
        Queue<Long> queue = new LinkedList<>();
        List<Long> delList = new LinkedList<>();
        List<NoteIndexUpdateLog> addList = new LinkedList<>();
        queue.add(parentId);

        //BFS Search
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            List<NoteIndex> noteList = noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().parentId(id).get().example());
            if (noteList.size() > 0) {
                queue.addAll(noteList.stream().map(NoteIndex::getId).collect(Collectors.toList()));
            }

            NoteIndexUpdateLog addLog = new NoteIndexUpdateLog();
            addLog.setIndexId(id);
            addLog.setType(NoteOpType.DELETE);
            addLog.setCreateTime(new Date());

            addList.add(addLog);
            delList.add(id);
        }
        noteIndexMapper.delByListIds(delList);
        noteIndexLogMapper.insertBatch(addList);
        log.info("delDir: 删除成功, 共计 {} 条数据", delList.size());

    }
}
