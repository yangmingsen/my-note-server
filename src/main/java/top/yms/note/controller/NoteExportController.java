package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteExportService;
import top.yms.note.entity.RestOut;
import top.yms.note.service.NoteFileService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/export")
public class NoteExportController {

    @Resource
    private NoteExportService noteExportService;

    @Resource
    private NoteFileService noteFileService;

    @GetMapping("/pdf")
    public RestOut<String> exportPdf(@RequestParam("noteId") Long noteId) throws Exception{
        String fileId = noteExportService.export(noteId, NoteConstants.PDF);
        String url = NoteConstants.getBaseUrl()+NoteConstants.getFileDownloadUrlSuffix(fileId);
        return RestOut.success(url);
    }

    @GetMapping("/docx")
    public RestOut<String> exportDocx(@RequestParam("noteId") Long noteId)throws Exception{
        String fileId = noteExportService.export(noteId, NoteConstants.DOCX);
        String url = NoteConstants.getBaseUrl()+NoteConstants.getFileDownloadUrlSuffix(fileId);
        return RestOut.success(url);
    }
}
