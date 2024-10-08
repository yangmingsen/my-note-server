package top.yms.note.controller;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStore;
import top.yms.note.conpont.NoteCache;
import top.yms.note.entity.NoteTree;
import top.yms.note.exception.BusinessException;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.WangEditorUploadException;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.service.NoteFileService;
import top.yms.note.service.NoteIndexService;
import top.yms.note.utils.LocalThreadUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@RestController
@RequestMapping("/file/")
public class NoteFileController {

    private static final Logger  log = LoggerFactory.getLogger(NoteFileController.class);

    @Autowired
    private NoteFileService noteFileService;

    @Autowired
    private FileStore fileStore;

    @Autowired
    private NoteFileMapper noteFileMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier(Constants.defaultNoteCache)
    private NoteCache noteCache;

    /**
     * 目前用于wangeditor的图片上传
     * @param file
     * @param id noteid
     * @return
     * @throws WangEditorUploadException
     */
    @PostMapping("/upload")
    public JSONObject uploadFileForWer(@RequestParam(value = "file") MultipartFile file,
                                       @RequestParam("id") Long id) throws WangEditorUploadException {
        log.info("uploadFileForWer: id={}", id);
        return noteFileService.uploadFileForWer(file, id);
    }

    /**
     * 目前用于markdown图片上传
     * @param file
     * @param id noteid
     * @return
     * @throws BusinessException
     */
    @PostMapping("/uploadV2")
    public RestOut<JSONObject> uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("id") Long id) throws BusinessException {
        JSONObject res = noteFileService.uploadFile(file, id);
        return RestOut.success(res);
    }


    @PostMapping("/uploadText")
    public RestOut<JSONObject> uploadText(@RequestBody Map<String, Object> dataMap) throws BusinessException {
        Long parentId = Long.parseLong((String)dataMap.get("parentId"));
        String text = (String)dataMap.get("content");
        if (StringUtils.isBlank(text)) {
            throw new BusinessException(NoteIndexErrorCode.E_203115);
        }
        JSONObject res = noteFileService.uploadText(text,parentId);

        return RestOut.success(res);
    }

    @PostMapping("/uploadTmpFile")
    public RestOut uploadTmpFile(@RequestParam(value = "file") MultipartFile file) throws Exception {
        if (file == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203108);
        }

        JSONObject resJson = new JSONObject();
        String fileId = fileStore.saveFile(file);
        String url = Constants.BASE_TMP_VIEW_URL+fileId;
        resJson.put("url", url);
        resJson.put("fileId", fileId);
        resJson.put("userId", LocalThreadUtils.getUserId().toString());
        resJson.put("createTime", new Date());
        resJson.put("type", file.getContentType());
        resJson.put("name", file.getName());
        resJson.put("size", file.getSize());

        //存入mongo
        Document document = Document.parse(resJson.toString());
        mongoTemplate.save(document, Constants.tmpUploadFile);

        return RestOut.success(resJson);
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
        note.setSize(file.getSize());
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
//        log.info("upload Note={}", note);
        noteFileService.addNote(file, note);

        return RestOut.success("ok");
    }

    //todo 加密访问先放着
    private boolean checkIsEncryptedNote(NoteFile noteFile, HttpServletRequest req, HttpServletResponse response, String msg) {
        NoteIndex noteIndex = null;
        if (noteFile.getNoteRef() != 0L) {
            noteIndex = noteIndexService.findOne(noteFile.getNoteRef());
        } else {
            noteIndex = noteIndexService.findBySiteId(noteFile.getFileId());
        }

        if ("1".equals(noteIndex.getEncrypted())) {
            String tmpToken = req.getParameter("tmpToken");
            if (StringUtils.isNotBlank(tmpToken)) {
                Long noteIdRef = noteFile.getNoteRef();
                if (noteIdRef == 0L) {
                    String fileId = noteFile.getFileId();
                    NoteIndex tmpNoteIndex = noteIndexService.findBySiteId(fileId);
                    noteIdRef = tmpNoteIndex.getId();
                }
                String cacheId = Constants.tmpReadPasswordToken+noteIdRef;
                String tmpTokenValue  = (String)noteCache.find(cacheId);
                if (StringUtils.isNotBlank(tmpTokenValue)) {
                    if (tmpTokenValue.equals(tmpToken)) {
                        //token正确
                        noteCache.delete(cacheId);
                        return true;
                    }
                }
            }
            log.info("checkIsEncryptedNote: 携带tmpToken: {}", tmpToken);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter writer=null;
            try {
                RestOut<Object> restOut = RestOut.error(msg);
                JSONObject resJson = JSONObject.from(restOut);
                writer=response.getWriter();
                writer.write(resJson.toString());
                writer.flush();
            }catch (Exception ex) {
                log.error("reponseErr", ex);
            }finally {
                if(writer!=null) {
                    writer.close();
                }
            }

            return false;
        }
        return true;
    }

    @Autowired
    private NoteIndexService noteIndexService;


    @GetMapping("/tmpView")
    public void tmpView(@RequestParam("id") String id, HttpServletResponse resp) throws Exception {
        log.info("tmpView: id={}", id);
        if (StringUtils.isBlank(id)) return;

        AnyFile file = fileStore.loadFile(id);
        resp.setContentType(file.getContentType());
        file.writeTo(resp.getOutputStream());
    }

    @GetMapping("/view")
    public void view(@RequestParam("id") String id, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        log.info("view: id={}", id);
        if (StringUtils.isBlank(id)) return;
        NoteFile noteFile = noteFileService.findOne(id);
//        log.info("view: noteFile:{}", noteFile);
        if (noteFile == null) return ;

//        if (!checkIsEncryptedNote(noteFile, req, resp, "加密笔记不可阅读")) {
//            return;
//        }

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
    public void download(@RequestParam("id") String id, HttpServletRequest req, HttpServletResponse resp)  throws Exception{
        log.info("download: id={}", id);
        if (StringUtils.isBlank(id)) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }

        NoteFile noteFile = noteFileService.findOne(id);
//        log.info("download: noteFile:{}", noteFile);
        if (noteFile == null) return ;

//        if (!checkIsEncryptedNote(noteFile, req, resp, "加密笔记不可下载")) {
//            return;
//        }

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


    @GetMapping("/url2pdf")
    public RestOut urlToPdf(@RequestParam("url") String url, @RequestParam("parentId") Long parentId) {
        log.info("urlToPdf: url={}", url);
        if (StringUtils.isBlank(url)) {
            throw new BusinessException(NoteIndexErrorCode.E_203116);
        }
        if (!url.startsWith("http")) {
            throw new BusinessException(CommonErrorCode.E_203005);
        }

        noteFileService.urlToPdf(url, parentId);
        return RestOut.succeed("ok");
    }


    //@GetMapping("/genTree")
    public RestOut genTree(@RequestParam("id") Integer id) throws Exception{
        String [] para = {
                "Github","tmp","专业","人体","公司",
                //0        1      2     3       4
                "其他分类","办公","团队协作","国家",
                //5         6       7           8
                "娱乐","学校","心理","总结","成长",
                //9     10      11      12      13
                "我","我的资源","数学","来自手机",
                //14  15        16      17
                "生活","经济",
                //18     19
        };
        //                   0     1       2     3      4      5       6          7     8        9        10
        Long parentId =1821940279838609210L;
        File file = new File("E:\\tmp\\youdaoNote\\yangmingsen\\"+para[id]);


        //last sync 5, next to 6
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


        noteFileService.generateTree(file, parentId);
        return RestOut.succeed("OK");
    }


    String baseSyncLocalPath = "E:\\tmp\\youdaoNote\\yangmingsen\\";

    @GetMapping("/syncNote")
    public RestOut syncNoteFromLocalFS(@RequestParam("id") Integer id) throws Exception {
        String [] para = {
                "Github","tmp","专业","人体","公司",
                //0        1      2     3       4
                "其他分类","办公","团队协作","国家",
                //5         6       7           8
                "娱乐","学校","心理","总结","成长",
                //9     10      11      12      13
                "我","我的资源","数学","来自手机",
                //14  15        16      17
                "生活","经济",
                //18     19
        };

        NoteTree rootNoteTree = noteIndexService.findCurUserRootNoteTree();

        String syncName = para[id];
        if (StringUtils.isBlank(syncName)) {
            throw new RuntimeException("syncName is empty");
        }
        NoteTree syncNoteTree = null;
        for (NoteTree nt : rootNoteTree.getChildren()) {
            if (syncName.equals(nt.getLabel())) {
                syncNoteTree = nt;
            }
        }
        if (syncNoteTree == null) {
            throw new RuntimeException("syncNoteTree is null");
        }

        String baseLocalPath = baseSyncLocalPath+syncName;
        File syncFile = new File(baseLocalPath);
        if (syncFile.listFiles() == null) {
            throw new RuntimeException("目标文件为空");
        }

        noteFileService.syncNoteFromLocalFS(syncNoteTree, syncFile);


        return RestOut.success("ok");
    }


}
