package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteSyncService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sync")
public class NoteSyncController {

    @Resource
    private NoteSyncService noteSyncService;

    @GetMapping("/chatNote")
    public RestOut<String> syncChatNote() {
        noteSyncService.syncChatNote();
        return RestOut.succeed();
    }
}
