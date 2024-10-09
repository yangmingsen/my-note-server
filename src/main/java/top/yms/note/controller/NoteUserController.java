package top.yms.note.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import top.yms.note.dto.NoteAuthPassword;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteAuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangmingsen on 2024/8/19.
 */
@RestController
@RequestMapping("/user")
public class NoteUserController {

    private static final Logger log = LoggerFactory.getLogger(NoteUserController.class);

    @Autowired
    private NoteAuthService noteAuthService;


    @PostMapping("/login")
    public RestOut login(@RequestBody NoteAuthPassword noteAuthPassword) {
        log.info("login: {}", noteAuthPassword);
        return noteAuthService.auth(noteAuthPassword);
    }
}
