package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteBookMarksService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/bookmarks")
public class NoteBookmarksController {

    @Resource
    private NoteBookMarksService noteBookMarksService;

    @GetMapping("/sync-local")
    public RestOut<String> syncBookMarks() throws Exception{
        noteBookMarksService.syncWithLocalBookmarks();
        return RestOut.succeed();
    }

    @GetMapping("/sync-note")
    public RestOut<String> syncBookmarksNote() throws Exception{
        noteBookMarksService.syncBookmarksNote();
        return RestOut.succeed();
    }
}
