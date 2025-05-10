package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteTag;
import top.yms.note.service.NoteTagService;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/tag")
public class NoteTagController {

    @Resource
    private NoteTagService noteTagService;

    @GetMapping("/findByUser")
    public List<NoteTag> findByUser() {
        return noteTagService.findByUser();
    }


    @GetMapping("/addTag")
    public Boolean addTag(NoteTag noteTag) {
        return noteTagService.addTag(noteTag);
    }

    @GetMapping("/findByTag")
    List<NoteIndex> findByTag(Long tagId) {
        return noteTagService.findByTag(tagId);
    }

    @GetMapping("/test")
    public List<String> list() {
        List<String> list = new LinkedList<>();
        for (int i=0; i< 10; i++) {
            list.add(i+"_"+i);
        }
        return list;
    }
}
