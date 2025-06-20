package top.yms.note.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.conpont.note.NotePreview;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.msgcd.NoteIndexErrorCode;
import top.yms.note.service.NoteDataService;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
        return RestOut.succeed();
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
        return RestOut.succeed();
    }

    @GetMapping("/get")
    public RestOut<NoteData> findOne(@RequestParam("id") Long id, HttpServletRequest request) {
        long st = System.currentTimeMillis();
        if (id == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        String tmpToken = request.getParameter(NoteConstants.TMP_TOKEN_FLAG);
        if (StringUtils.isNotBlank(tmpToken)) {
            log.debug("tmpToken={}", tmpToken);
            LocalThreadUtils.get().put(NoteConstants.TMP_VISIT_TOKEN, tmpToken);
        }
        NoteData res = noteDataService.findNoteData(id);
        long et = System.currentTimeMillis();
        log.debug("findOne spend time = {}", et-st);
        return RestOut.success(res);
    }


    @GetMapping("/checkFileCanPreview")
    public RestOut<String> checkFileCanPreview(@RequestParam("id") Long id) {
        boolean canPreview = notePreview.canPreview(id);
        if (canPreview) {
            return RestOut.succeed();
        }
        return RestOut.error(BusinessErrorCode.E_204008);
    }


    @GetMapping("/index-rebuild")
    public RestOut<String> indexRebuild() {
        noteDataIndexService.rebuildIndex();
        return RestOut.succeed();
    }

}
