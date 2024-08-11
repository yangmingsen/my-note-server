package top.yms.note.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.entity.AntTreeNode;
import top.yms.note.exception.BusinessException;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteTree;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteIndexService;
import top.yms.note.utils.LocalThreadUtils;
import top.yms.note.vo.MenuListVo;
import top.yms.note.vo.NoteInfoVo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@RestController
@RequestMapping("/note-index")
public class NoteIndexController {

    private Logger log = LoggerFactory.getLogger(NoteIndexController.class);

    @Autowired
    private NoteIndexService noteIndexService;

    @GetMapping("/list")
    public RestOut<List<NoteIndex>> findByUid() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        log.info("findByUid: {}", uid);
        List<NoteIndex> noteList = noteIndexService.findByUserId(uid);
        log.info("findByUid: {} , count: {}", uid, noteList.size());
        return RestOut.success(noteList);
    }

    @GetMapping("/tree")
    public RestOut findNoteTreeByUid() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        log.info("findNoteTreeByUid: {}", uid);
        List<NoteTree> noteTreeList = noteIndexService.findNoteTreeByUid(uid);
        log.info("findByUid: {} , count: {}", uid, noteTreeList.size());
        return RestOut.success(noteTreeList);
    }

    @GetMapping("/antTree")
    public RestOut findAntTree() {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        log.info("antTree: {}", uid);
        List<NoteTree> noteTreeList = noteIndexService.findNoteTreeByUid(uid);
        List<AntTreeNode> antTreeList = new LinkedList<>();
        for (NoteTree noteTree : noteTreeList) {
            antTreeList.add(noteIndexService.transferToAntTree(noteTree));
        }

        log.info("antTree: {} , count: {}", uid, antTreeList.size());
        return RestOut.success(antTreeList);
    }

    /**
     * 根据 uid和nid找 列表
     * @param parentId
     * @return
     */
    @GetMapping("/sub")
    public RestOut<List<NoteIndex>> findSubBy(@RequestParam("nid") Long parentId) {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        if (uid == null || parentId==null) {
            throw new BusinessException(CommonErrorCode.E_100101);
        }
        log.info("findSubBy: uid= {}, parentId={}", uid, parentId);
        List<NoteIndex> resList =  noteIndexService.findSubBy(parentId, uid);
        log.info("findSubBy: uid= {}, parentId={}, count:{}", uid, parentId, resList.size());

        return RestOut.success(resList);
    }

    @GetMapping("/findRoot")
    public RestOut<NoteIndex> findRootNoteIndex() {
        return RestOut.success(noteIndexService.findRoot());
    }

    /**
     * 需求： 从子层返回上一层。
     * <p>使用当前层的id找到parentId, 然后根据parentId找到所有该parentId下面的子节点</p>
     * @param id
     * @return
     */
    @GetMapping("/findBackParentDir")
    public RestOut<List<NoteIndex>> findBackParentDir(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203104);
        }
        List<NoteIndex> resList =  noteIndexService.findBackParentDir(id);
        log.info("findBackParentDir: id= {},  count:{}", id, resList.size());

        return RestOut.success(resList);
    }

    @GetMapping("/findOne")
    public RestOut<NoteIndex> findOne(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203104);
        }
        NoteIndex res = noteIndexService.findOne(id);
        log.info("findOne: id= {},  count:{}", id, res);

        return RestOut.success(res);
    }


    @GetMapping("/menuList")
    public RestOut<MenuListVo> findMenuList(@RequestParam("nid") Long nid) {
        //0->dir(menu); 1->file(content)
        Map<String, List<NoteIndex>>  mapList = findSubBy(nid).getResult().orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(NoteIndex::getIsile));
        MenuListVo res = new MenuListVo();
        res.setMenuList(mapList.get("0"));
        res.setNoteContentMenuList(mapList.get("1"));

        return RestOut.success(res);
    }


    @PostMapping("add")
    public RestOut<String> add(@RequestBody NoteIndex node) {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        node.setUserId(uid);
        log.info("add: {}", node);
        if (node == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        if (StringUtils.isBlank(node.getName())) {
            throw new BusinessException(NoteIndexErrorCode.E_203100);
        }
        if (StringUtils.isBlank(node.getIsile())) {
            throw new BusinessException(NoteIndexErrorCode.E_203102);
        }
        if ("1".equals(node.getIsile()) && StringUtils.isBlank(node.getType())) {
            throw new BusinessException(NoteIndexErrorCode.E_203103);
        }
        noteIndexService.add(node);

        return RestOut.success("Ok");
    }

    @PostMapping("/update")
    public RestOut<String> update(@RequestBody NoteIndex note) {
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        note.setUserId(uid);
        log.info("update: {}", note);
        if (note == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        if (note.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }
        noteIndexService.update(note);
        return RestOut.succeed("ok");
    }


    @GetMapping("/delDir")
    public RestOut<String> delDir(@RequestParam("parentId") Long parentId) {
        log.info("delDir: parentId={}", parentId);
        if (parentId == null) {
            throw new BusinessException(CommonErrorCode.E_203000);
        }
        noteIndexService.delDir(parentId);
        return RestOut.succeed("ok");
    }


    @PostMapping("/delNote")
    public RestOut<String> delNote(@RequestBody NoteIndex note) {
        log.info("delNote: {}", note);
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        note.setUserId(uid);
        if (note.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }

        noteIndexService.delNote(note.getId());
        return RestOut.succeed("ok");
    }

    @GetMapping("/destroyNote")
    public RestOut<String> destroyNote(@RequestParam("id") Long id) {
        log.info("destroyNote id= {}", id);
        noteIndexService.destroyNote(id);
        return RestOut.succeed("ok");
    }

    @PostMapping("/findBy")
    public RestOut<List<NoteIndex>> findBy(@RequestBody NoteIndexQuery query) {
        log.info("findBy: {}", query);
        if (query == null) {
            throw new BusinessException(CommonErrorCode.E_100101);
        }
        List<NoteIndex> resList = noteIndexService.findBy(query);

        log.info("findBy Result: 共{}条", resList.size());
        return RestOut.success(resList);
    }

    @GetMapping("/findBreadcrumb")
    public RestOut<List<NoteIndex>> findBreadcrumb (@RequestParam("id") Long id) {
        log.info("findBreadcrumb: id={}", id);
        if (id == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }
        List<NoteIndex> res = new LinkedList<>();
        noteIndexService.findBreadcrumb(id, res);
        log.info("findBreadcrumb Result: 共{}条", res.size());
        return RestOut.success(res);
    }

    @GetMapping("/getNoteAndSite")
    public RestOut<NoteInfoVo> getNoteAndSite(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }
        NoteInfoVo res = noteIndexService.getNoteAndSite(id);
        return RestOut.success(res);
    }

    @GetMapping("/getRecentFiles")
    public RestOut<List<NoteIndex>> getRecentFiles() {
        List<NoteIndex> resList = noteIndexService.getRecentFiles();
        log.info("getRecentFiles Result: 共{}条", resList.size());

        return RestOut.success(resList);
    }

    @GetMapping("/getDeletedFiles")
    public RestOut<List<NoteIndex>> getDeletedFiles() {
        List<NoteIndex> resList = noteIndexService.getDeletedFiles();
        log.info("getDeletedFiles Result: 共{}条", resList.size());

        return RestOut.success(resList);
    }


    @GetMapping("/allDestroy")
    public RestOut allDestroy() {
        int cnt = noteIndexService.allDestroy();
        log.info("allDestroy: {}", cnt);
        return RestOut.succeed("OK");
    }

    @GetMapping("/allRecover")
    public RestOut allRecover() {
        int cnt = noteIndexService.allRecover();
        log.info("allRecover: {}", cnt);
        return RestOut.succeed("OK");
    }
}
