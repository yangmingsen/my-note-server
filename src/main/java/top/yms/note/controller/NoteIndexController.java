package top.yms.note.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.yms.note.comm.BusinessException;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteTree;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteIndexService;
import top.yms.note.utils.IdWorker;
import top.yms.note.vo.MenuListVo;

import java.util.Collections;
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



    @GetMapping("/{uid}")
    public RestOut<List<NoteIndex>> findByUid(@PathVariable Long uid) {
        log.info("findByUid: {}", uid);
        List<NoteIndex> noteList = noteIndexService.findByUserId(uid);
        log.info("findByUid: {} , count: {}", uid, noteList.size());
        return RestOut.success(noteList);
    }


    @GetMapping("/tree")
    public RestOut<List<NoteTree>> findNoteTreeByUid(@RequestParam("uid") Long uid) {
        log.info("findNoteTreeByUid: {}", uid);
        List<NoteTree> noteTreeList = noteIndexService.findNoteTreeByUid(uid);
        log.info("findByUid: {} , count: {}", uid, noteTreeList.size());
        return RestOut.success(noteTreeList);
    }

    /**
     * 根据 uid和nid找 列表
     * @param uid
     * @param parentId
     * @return
     */
    @GetMapping("/sub")
    public RestOut<List<NoteIndex>> findSubBy(@RequestParam("uid") Long uid,
                                              @RequestParam("nid") Long parentId) {
        if (uid == null || parentId==null) {
            throw new BusinessException(CommonErrorCode.E_100101);
        }
        log.info("findSubBy: uid= {}, parentId={}", uid, parentId);
        List<NoteIndex> resList =  noteIndexService.findSubBy(parentId, uid);
        log.info("findSubBy: uid= {}, parentId={}, count:{}", uid, parentId, resList.size());

        return RestOut.success(resList);

    }

    @GetMapping("/menuList")
    public RestOut<MenuListVo> findMenuList(@RequestParam("uid") Long uid,
                                            @RequestParam("nid") Long nid) {
        //0->dir(menu); 1->file(content)
        Map<String, List<NoteIndex>>  mapList = findSubBy(uid, nid).getResult().orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(NoteIndex::getIsile));
        MenuListVo res = new MenuListVo();
        res.setMenuList(mapList.get("0"));
        res.setNoteContentMenuList(mapList.get("1"));

        return RestOut.success(res);
    }


    @PostMapping("add")
    public RestOut<String> add(@RequestBody NoteIndex node) {
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
    public RestOut<String> update(@RequestBody NoteIndex node) {
        log.info("update: {}", node);
        if (node == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        if (node.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }

        noteIndexService.update(node);


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

}
