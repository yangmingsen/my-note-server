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
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.LocalThreadUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

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

    @Autowired
    private NoteFileMapper noteFileMapper;

    @PostMapping("/upload")
    public JSONObject uploadFileForWer(@RequestParam(value = "file") MultipartFile file) throws WangEditorUploadException {
        return noteFileService.uploadFileForWer(file);
    }

    @PostMapping("/uploadV2")
    public RestOut<JSONObject> uploadFile(@RequestParam("file") MultipartFile file) throws BusinessException {
        JSONObject res = noteFileService.uploadFile(file);
        return RestOut.success(res);
    }


    @PostMapping("/uploadText")
    public RestOut<JSONObject> uploadText(@RequestBody Map<String, Object> dataMap) throws BusinessException {
        Long parentId = Long.parseLong((String)dataMap.get("parentId"));
        String text = (String)dataMap.get("content");
        if (parentId== null) {
            throw new BusinessException(NoteIndexErrorCode.E_203105);
        }
        if (StringUtils.isBlank(text)) {
            throw new BusinessException(NoteIndexErrorCode.E_203115);
        }


        JSONObject res = noteFileService.uploadText(text,parentId);

        return RestOut.success(res);
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

        //update viewCount
        NoteFile upCnt = new NoteFile();
        upCnt.setId(noteFile.getId());
        upCnt.setViewCount(noteFile.getViewCount()+1);
        noteFileMapper.updateByPrimaryKeySelective(upCnt);

        AnyFile file = fileStore.loadFile(id);
        resp.setContentType(file.getContentType());
        file.writeTo(resp.getOutputStream());

    }

    @GetMapping("/download")
    public void download(@RequestParam("id") String id, HttpServletResponse resp)  throws Exception{
        log.info("download: id={}", id);
        if (StringUtils.isBlank(id)) return;
        NoteFile noteFile = noteFileService.findOne(id);
        log.info("download: noteFile:{}", noteFile);
        if (noteFile == null) return ;
        NoteFile upCnt = new NoteFile();
        upCnt.setId(noteFile.getId());
        upCnt.setDownloadCount(noteFile.getDownloadCount()+1);
        noteFileMapper.updateByPrimaryKeySelective(upCnt);

        AnyFile file = fileStore.loadFile(id);

        resp.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(file.getFilename(), "UTF-8") + "\"");
        resp.addHeader("Content-Length", "" + file.getLength());
        resp.setContentType(file.getContentType());
        file.writeTo(resp.getOutputStream());
    }


//    @GetMapping("/genTree")
//    public RestOut genTree(@RequestParam("id") Integer id) throws Exception{
//        //String [] para = {};
//        //                   0     1       2     3      4      5       6          7     8        9        10
//        //Long parentId =1821940279838609210L;
//        //File file = new File("E:\\tmp\\youdaoNote\\yangmingsen\\"+para[id]);
//
//
//        //last sync 5, next to 6
//        String [] fileNames = {
//                "CI&CD","专业常识","业务问题","个人设计","其他","分布式","加密技术","复习原则.md","大数据",
//                // 0        1          2         3          4      5        6           7            8
//                "安全认证","工具","技术","技术学习原则.md","操作系统","数据库","数据结构与算法","标准","概念",
//                //   9      10      11       12               13          14             15        16    17
//                "算法题","编程语言","编译原理","网络","解决方案","软件工程","软件设计","项目开发"
//                //18           19       20       21        22          23          24     25
//        };
//        Long parentId = 1822940969633329353L;
//        File file = new File("E:\\tmp\\youdaoNote\\yangmingsen\\专业\\"+fileNames[id]);
//
//
//        noteFileService.generateTree(file, parentId);
//        return RestOut.succeed("OK");
//    }


}
