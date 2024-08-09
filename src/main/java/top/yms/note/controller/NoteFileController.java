package top.yms.note.controller;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStore;
import top.yms.note.exception.BusinessException;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.WangEditorUploadException;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.LocalThreadUtils;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@RestController
@RequestMapping("/file/")
public class NoteFileController {

    private Logger log = LoggerFactory.getLogger(NoteFileController.class);

    @Autowired
    private NoteFileService noteFileService;

    @Autowired
    private FileStore fileStore;

    @PostMapping("/upload")
    public JSONObject uploadFileForWer(@RequestParam(value = "file") MultipartFile file) throws WangEditorUploadException {
        return noteFileService.uploadFileForWer(file);
    }


    @PostMapping("/uploadNote")
    public RestOut uploadNote(@RequestParam(value = "file") MultipartFile file,
                              @RequestParam("parentId") Long parentId) throws Exception {
        if (file == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203108);
        }
        if (parentId== null) {
            throw new BusinessException(NoteIndexErrorCode.E_203105);
        }
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException(NoteIndexErrorCode.E_203109);
        }

        NoteIndex note = new NoteIndex();
        note.setParentId(parentId);
        note.setUserId(uid);
        note.setName(fileName);
        note.setIsile("1");
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            int len = fileName.length();
            //获取文件后缀
            String fileType = fileName.substring(dot + 1, len).toLowerCase();
            note.setType(fileType);
        } else {
            note.setType(FileTypeEnum.UNKNOWN.getValue());
        }
        note.setCreateTime(new Date());
        log.info("upload Note={}", note);
        noteFileService.addNote(file, note);

        return RestOut.success("ok");
    }

    @GetMapping("/view")
    public void view(@RequestParam("id") String id, HttpServletResponse resp) throws Exception {
        log.info("view: id={}", id);
        if (StringUtils.isBlank(id)) return;
        NoteFile noteFile = noteFileService.findOne(id);
        log.info("view: noteFile:{}", noteFile);
        if (noteFile == null) return ;
        AnyFile file = fileStore.loadFile(id);
        resp.setContentType(file.getContentType());
        file.writeTo(resp.getOutputStream());

    }

    @GetMapping("/download")
    public void download(@RequestParam("id") String id, HttpServletResponse resp)  throws Exception{
        AnyFile file = fileStore.loadFile(id);

        resp.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(file.getFilename(), "UTF-8") + "\"");
        resp.addHeader("Content-Length", "" + file.getLength());

        resp.setContentType(file.getContentType());
        file.writeTo(resp.getOutputStream());
    }


}
