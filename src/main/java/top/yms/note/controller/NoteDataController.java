package top.yms.note.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yms.note.exception.BusinessException;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteDataService;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@RestController
@RequestMapping("/note-data")
public class NoteDataController {

    private static Logger log = LoggerFactory.getLogger(NoteDataController.class);

    @Autowired
    private NoteDataService noteDataService;

    @PostMapping("/addAndUpdate")
    public RestOut<String> addAndUpdate(@RequestBody NoteData noteData) {
        log.info("addAndUpdate: noteData={}", noteData);
        if (noteData == null) {
            throw new BusinessException(CommonErrorCode.E_100101);
        }
        if (noteData.getId() == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203104);
        }
        noteDataService.addAndUpdate(noteData);
        return RestOut.succeed("ok");
    }

    @GetMapping("/get")
    public RestOut get(@RequestParam("id") Long id) {
        log.info("get: id={}", id);
        NoteData res = noteDataService.get(id);
        return RestOut.success(res);
    }
}
