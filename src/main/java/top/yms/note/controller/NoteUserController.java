package top.yms.note.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.dto.NoteAuthPassword;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteAuthService;

import javax.annotation.Resource;

/**
 * Created by yangmingsen on 2024/8/19.
 */
@RestController
@RequestMapping("/user")
public class NoteUserController {

    private static final Logger log = LoggerFactory.getLogger(NoteUserController.class);

    @Resource
    private NoteAuthService noteAuthService;


    @PostMapping("/login")
    public RestOut login(@RequestBody NoteAuthPassword noteAuthPassword) {
        log.debug("login: {}", noteAuthPassword);
        return noteAuthService.auth(noteAuthPassword);
    }
}
