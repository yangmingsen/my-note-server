package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.NoteShareService;
import top.yms.note.entity.RestOut;
import top.yms.note.vo.NoteShareVo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/share")
public class ShareController {

    @Resource
    private NoteShareService noteShareService;

    @GetMapping("/get")
    RestOut<NoteShareVo> shareNoteGet(@RequestParam("noteId") Long noteId) {
        NoteShareVo noteShareVo = noteShareService.shareNoteGet(noteId);
        return RestOut.success(noteShareVo);
    }

    @GetMapping("/resource/view")
    public void shareResource(@RequestParam("id") String id, HttpServletResponse resp) throws Exception{
        AnyFile file = noteShareService.shareResource(id);
        resp.setContentType(file.getContentType());
        file.writeTo(resp.getOutputStream());
    }

}
