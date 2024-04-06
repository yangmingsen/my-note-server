package top.yms.note.controller;

import com.alibaba.fastjson2.JSONObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.entity.NoteFile;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.MongoDB;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@RestController
@RequestMapping("/file/")
public class NoteFileController {

    private Logger log = LoggerFactory.getLogger(NoteFileController.class);

    @Autowired
    private NoteFileService noteFileService;

    @PostMapping("/upload")
    public JSONObject uploadFileForWer(@RequestParam(value = "file") MultipartFile file) throws Exception{
        return noteFileService.uploadFileForWer(file);
    }

    @GetMapping("/view")
    public void view(@RequestParam("id") String id, HttpServletResponse resp) throws Exception {
        log.info("view: id={}", id);
        if (StringUtils.isBlank(id)) return;
        NoteFile noteFile = noteFileService.findOne(id);
        log.info("view: noteFile:{}", noteFile);
        if (noteFile == null) return ;
        GridFSDBFile file = MongoDB.loadFile(id);
        resp.setContentType(noteFile.getType());
        file.writeTo(resp.getOutputStream());

    }

    @GetMapping("/download")
    public void download(@RequestParam("id") String id, HttpServletResponse resp)  throws Exception{
        GridFSDBFile file = MongoDB.loadFile(id);
        resp.setContentType("application/octet-stream");
        resp.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(file.getFilename(), "UTF-8") + "\"");
        resp.addHeader("Content-Length", "" + file.getLength());

        resp.setContentType(file.getContentType());
        file.writeTo(resp.getOutputStream());
    }


    @GetMapping("/view1")
    public void view1(@RequestParam("id") String id, HttpServletResponse resp)  throws Exception{
        log.info("view1: id={}", id);
        GridFSDBFile file = MongoDB.loadFile(id);
        resp.setContentType(MediaType.IMAGE_JPEG_VALUE);
        file.writeTo(resp.getOutputStream());

//        resp.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(file.getFilename(), "UTF-8") + "\"");
//        resp.addHeader("Content-Length", "" + file.getLength());

    }




}
