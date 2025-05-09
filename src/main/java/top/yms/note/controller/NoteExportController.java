package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteExportService;
import top.yms.note.service.NoteFileService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/export")
public class NoteExportController {

    @Resource
    private NoteExportService noteExportService;

    @Resource
    private NoteFileService noteFileService;

    @GetMapping("/pdf")
    public void exportPdf(@RequestParam("noteId") Long noteId,
                          HttpServletRequest request, HttpServletResponse response) throws Exception{
        String fileId = noteExportService.export(noteId, NoteConstants.PDF);
        noteFileService.download(fileId, request, response);
    }
}
