package top.yms.note.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.conpont.note.NotePreview;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.BusinessException;
import top.yms.note.service.NoteDataService;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@RestController
@RequestMapping("/note-data")
public class NoteDataController {

    private static final Logger log = LoggerFactory.getLogger(NoteDataController.class);

    @Resource
    private NoteDataService noteDataService;

    @Resource
    private NoteDataIndexService noteDataIndexService;

    @Resource
    private NotePreview notePreview;


    @PostMapping("/mindmapSave")
    public RestOut mindMapSave(@RequestParam("id") Long id, @RequestParam("content") String jsonContent) {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        if (id == null || StringUtils.isBlank(jsonContent)) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        log.debug("id = {}, content = {}", id, jsonContent);
        NoteDataDto noteDataDto = new NoteDataDto();
        noteDataDto.setId(id);
        noteDataDto.setContent(jsonContent);
        noteDataDto.setUserId(uid);
        noteDataService.save(noteDataDto);
        return RestOut.success("Ok");
    }


    @PostMapping("/addAndUpdate")
    public RestOut<String> addAndUpdate(@RequestBody NoteDataDto noteData) {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        noteData.setUserId(uid);
        log.debug("addAndUpdate: noteData={}", noteData);
        if (noteData.getId() == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203104);
        }
        noteDataService.save(noteData);
        return RestOut.succeed("ok");
    }

    @GetMapping("/get")
    public RestOut findOne(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        NoteData res = noteDataService.findNoteData(id);
        return RestOut.success(res);
    }


    @GetMapping("/checkFileCanPreview")
    public RestOut checkFileCanPreview(@RequestParam("id") Long id) {
        boolean canPreview = notePreview.canPreview(id);
        if (canPreview) {
            return RestOut.succeed("Ok");
        }
        return RestOut.error("Not Support");
    }


    @GetMapping("/index-rebuild")
    public RestOut indexRebuild() {
        noteDataIndexService.rebuildIndex();

        return RestOut.succeed("Ok");
    }

}
