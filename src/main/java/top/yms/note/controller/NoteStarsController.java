package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.entity.NoteMeta;
import top.yms.note.service.NoteStarsService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/stars")
public class NoteStarsController {
    @Resource
    private NoteStarsService noteStarsService;

    @GetMapping("/findByUser")
    public List<NoteMeta> findByUser() {
        return noteStarsService.findByUser();
    }

    @GetMapping("/addStar")
    public Boolean addStar(Long noteId) {
        return noteStarsService.addStar(noteId);
    }
}
