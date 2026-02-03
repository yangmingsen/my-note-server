package top.yms.note.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.NoteFetchService;
import top.yms.note.dao.NoteFileQuery;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteTree;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.exception.WangEditorUploadException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteFileService;
import top.yms.note.service.NoteMetaService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;
import top.yms.note.vo.LocalNoteSyncResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Service
public class NoteFileServiceImpl implements NoteFileService {

    private static final Logger log = LoggerFactory.getLogger(NoteFileServiceImpl.class);

    @Resource
    private NoteFileMapper noteFileMapper;

    @Resource
    private NoteMetaService noteMetaService;

    @Resource
    private IdWorker idWorker;

    @Resource
    private NoteDataService noteDataService;

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private NoteDataMapper noteDataMapper;

    @Resource
    private NoteFetchService noteFetchService;

    /**
     * <p>上传成功的返回格式：</p>
     * <code>
     *    {
     *     "errno": 0, // 注意：值是数字，不能是字符串
     *     "data": {
     *         "url": "xxx", // 图片 src ，必须
     *         "alt": "yyy", // 图片描述文字，非必须
     *         "href": "zzz" // 图片的链接，非必须
     *     }
     * }
     * </code>
     *
     * <p>上传失败的返回格式：</p>
     * <code>
     *     {
     *     "errno": 1, // 只要不等于 0 就行
     *     "message": "失败信息"
     * }
     * </code>
     *
     * @param file
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public JSONObject uploadFileForWer(MultipartFile file, Long noteId) throws WangEditorUploadException {
        JSONObject res = new JSONObject();
        Map<String, Object> reqInfo = LocalThreadUtils.get();
        long userId = (long)reqInfo.get(NoteConstants.USER_ID);
        log.debug("uploadFileForWer: userId={}", userId);
        String fileId = null;
        try {
            fileId = fileStoreService.saveFile(file);
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();
            long fileSize = file.getSize();
            String tmpViewUrl = NoteConstants.getFileViewUrlSuffix(fileId);
            String url = NoteConstants.getBaseUrl()+tmpViewUrl;
            NoteFile noteFile = new NoteFile();
            noteFile.setFileId(fileId);
            noteFile.setName(fileName);
            noteFile.setType(fileType);
            noteFile.setSize(fileSize);
            noteFile.setUserId(userId);
            noteFile.setUrl(tmpViewUrl);
            noteFile.setNoteRef(noteId);
            noteFile.setCreateTime(new Date());
            add(noteFile);
            //noteFileMapper.insertSelective(noteFile);
            JSONObject data = new JSONObject();
            data.put("url", url);
            data.put("alt", fileName);
            data.put("href", url);
            res.put("data", data);
            res.put("errno", 0);
            return res;
        } catch (Exception e) {
            if (fileId != null) {
                fileStoreService.delFile(fileId);
            }
            log.error("uploadFileForWer Error", e);
            throw new WangEditorUploadException(CommonErrorCode.E_203002);
        }

    }

    /**
     * 根据mongoFileId进行查找
     * @param fileId
     * @return
     */
    public NoteFile findOne(String fileId) {
        List<NoteFile> noteFiles = noteFileMapper.selectByExample(NoteFileQuery.Builder.build().fileId(fileId).get().example());
        if (noteFiles != null && noteFiles.size() >0) {
            return noteFiles.get(0);
        }
        return null;
    }

    private void handleMarkdown(MultipartFile file, NoteMeta note) throws Exception {
        long genId = idWorker.nextId();
        note.setStoreSite(NoteConstants.MYSQL);
        note.setId(genId);
        //存储到t_note_index
        noteMetaService.add(note);
        //bug20251116 上传markdown文档时出现乱码
        /*
        StringBuilder sb = new StringBuilder();
        try(InputStreamReader isr = new InputStreamReader(file.getInputStream())) {
            int bufLen = 1024;
            char [] cBuf = new char[bufLen];
            int rLen;
            while ((rLen = isr.read(cBuf)) > 0) {
                sb.append(new String(cBuf, 0, rLen));
            }
        }catch (Exception e) {
            log.error("读取mongo文件内容出错", e);
        }
         */
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        NoteDataDto noteData = new NoteDataDto();
        noteData.setId(genId);
        noteData.setUserId(note.getUserId());
        noteData.setContent(content);
        noteDataService.save(noteData);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 600)
    public void uploadMultiNote(Long rootParentId, List<MultipartFile> files) throws Exception {
        //备份rollbackList
        List<String> rollbackList = new ArrayList<>();
        try {
            Long userId = LocalThreadUtils.getUserId();
            Map<String, Long> pathToId = new HashMap<>();
            for (MultipartFile file : files) {
                // 获取相对路径（重点）
                String relativePath = file.getOriginalFilename(); // 自动带上 webkitRelativePath
                if (relativePath == null) continue;
                Path path = Paths.get(relativePath);
                Long parentId = rootParentId;
                // 依次处理目录结构
                for (int i = 0; i < path.getNameCount(); i++) {
                    String currentName = path.getName(i).toString();
                    String subPath = path.subpath(0, i + 1).toString();
                    if (i < path.getNameCount() - 1) { // 是目录
                        if (!pathToId.containsKey(subPath)) {
                            Long dirId = idWorker.nextId();
                            pathToId.put(subPath, dirId);
                            NoteMeta dir = new NoteMeta();
                            dir.setId(dirId);
                            dir.setParentId(parentId);
                            dir.setName(currentName);
                            dir.setIsFile("0");
                            dir.setUserId(userId);
                            dir.setCreateTime(new Date());
                            dir.setUpdateTime(new Date());
                            dir.setType(null);
                            //add noteMeta
                            noteMetaService.add(dir);
                            parentId = dirId;
                        } else {
                            parentId = pathToId.get(subPath);
                        }
                    } else { // 是文件
                        Long fileId = idWorker.nextId();
                        NoteMeta fileNote = new NoteMeta();
                        fileNote.setId(fileId);
                        fileNote.setParentId(parentId);
                        fileNote.setName(currentName);
                        fileNote.setUserId(userId);
                        fileNote.setCreateTime(new Date());
                        fileNote.setUpdateTime(new Date());
                        fileNote.setIsFile("1");
                        fileNote.setSize(file.getSize());
                        int dotIndex = currentName.lastIndexOf('.');
                        if (dotIndex != -1) {
                            fileNote.setType(currentName.substring(dotIndex + 1));
                        } else {
                            fileNote.setType("unknown");
                        }
                        //add note
                        addNote(file, fileNote);
                        String siteId = fileNote.getSiteId();
                        if (StringUtils.isNotBlank(siteId)) {
                            rollbackList.add(siteId);
                        }
                    }
                }
            }
            log.info("rollbackList={}", rollbackList);
        } catch (Exception e) {
            log.error("uploadMultiNote error", e);
            try {
                if (!rollbackList.isEmpty()) {
                    for (String fileId : rollbackList) {
                        fileStoreService.delFile(fileId);
                    }
                }
            } catch (Exception e1) {
                log.error("uploadMultiNote rollback error", e1);
            }
            throw e;
        }

    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 600)
    public NoteMeta addNote(MultipartFile file, NoteMeta note) throws Exception {
        String fileId = null;
        try {
            if (NoteConstants.markdownSuffix.equals(note.getType())) {
                handleMarkdown(file, note);
                return note;
            }
            fileId = fileStoreService.saveFile(file);
            //先默认上传到mongo
            note.setStoreSite(NoteConstants.MONGO);
            note.setSiteId(fileId);
            //存储到t_note_index
            noteMetaService.add(note);
            String tmpViewUrl = NoteConstants.getFileViewUrlSuffix(fileId);
            //store to t_note_file
            NoteFile noteFile = new NoteFile();
            noteFile.setFileId(fileId);
            noteFile.setName(note.getName());
            noteFile.setType(note.getType());
            noteFile.setSize(file.getSize());
            noteFile.setUserId(note.getUserId());
            noteFile.setUrl(tmpViewUrl);
            noteFile.setViewCount(0L);
            noteFile.setCreateTime(new Date());
            noteFile.setNoteRef(note.getId());
            add(noteFile);
            //noteFileMapper.insertSelective(noteFile);
            // return
            return note;
        } catch (Exception e) {
            if (fileId != null) {
                fileStoreService.delFile(fileId);
            }
            throw e;
        }
    }

    /**
     * 从本地文件同步到笔记系统.
     *
     * 注意： 这里的file必须是文件夹。
     *
     * 这里noteTree与file保持同层, 创建新文件交给generateTree.
     *     如果涉及到比对，再看...
     * @param noteTree
     * @param file 这里的file必须是文件夹, 如果是普通file, 会直接忽略
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 120)
    public void syncNoteFromLocalFS(NoteTree noteTree, File file, List<String> mongoRollBackList, List<LocalNoteSyncResult> syncStatisticList) throws Exception{
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            Map<String, NoteTree> treeMap = noteTree.getChildren().stream().collect(Collectors.toMap(NoteTree::getLabel, Function.identity(), (k1, k2) -> k2));
            for (File tf : files) {
                if (tf.getName().equals("images")) {
                    continue;
                }
                String fileName = tf.getName();
                NoteTree tmpNoteTree = treeMap.get(fileName);
                if (tmpNoteTree == null) {
                    log.debug("本地笔记[{}] 在TreeNote中不存在, 执行创建", tf.getAbsolutePath());
                }
                if (tmpNoteTree != null) {
                    //存在的情况, 继续往下处理
                    syncNoteFromLocalFS(tmpNoteTree, tf, mongoRollBackList, syncStatisticList);
                } else {
                    //不存在就创建
                    generateTree(tf, noteTree.getId(), mongoRollBackList, syncStatisticList);
                }

            }
        }
    }

    /**
     * 从本地文件导入, 主要用于创建新文件或文件夹
     * @param file 当前需要创建的文件/文件夹
     * @param parentId  file 的 parentId
     * @throws Throwable
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 120)
    public void generateTree(File file, Long parentId, final List<String> mongoRollBackList, List<LocalNoteSyncResult> syncStatisticList) throws Exception{
        if (file.getName().equals("images")) {
            return;
        }
        Long userId = LocalThreadUtils.getUserId();
        NoteMeta noteMeta = new NoteMeta();
        long id = idWorker.nextId();
        noteMeta.setId(id);
        noteMeta.setParentId(parentId);
        noteMeta.setUserId(userId);
        boolean isFile = file.isFile();
        if (isFile) {
            noteMeta.setIsFile("1");
        } else {
            noteMeta.setIsFile("0");
        }
        String fileName = file.getName();
        noteMeta.setName(fileName);
        noteMeta.setCreateTime(new Date());
        if (isFile) {
            int dot = fileName.lastIndexOf('.');
            if (dot > 0) {
                int len = fileName.length();
                //获取文件后缀
                String fileType = fileName.substring(dot + 1, len).toLowerCase();
                if (fileType.length() > 10) {
                    noteMeta.setType(FileTypeEnum.UNKNOWN.getValue());
                } else {
                    noteMeta.setType(fileType);
                }
            } else {
                noteMeta.setType(FileTypeEnum.UNKNOWN.getValue());
            }
            if (NoteConstants.markdownSuffix.equals(noteMeta.getType())) {
                noteMeta.setStoreSite(NoteConstants.MYSQL);
                StringBuilder sb = new StringBuilder();
                try(InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    int bufLen = 1024;
                    char [] cBuf = new char[bufLen];
                    int rLen = 0;
                    while ((rLen = isr.read(cBuf)) > 0) {
                        sb.append(new String(cBuf, 0, rLen));
                    }
                }catch (Exception e) {
                    log.error("读取mongo文件内容出错", e);
                }
                String contentStr = sb.toString();
                if (StringUtils.isNotBlank(contentStr))
                    contentStr = replaceImageUrls(contentStr, file, id, mongoRollBackList);
                NoteData noteData = new NoteData();
                noteData.setId(id);
                noteData.setUserId(userId);
                noteData.setContent(contentStr);
                noteData.setCreateTime(new Date());
                //设置内容大小
                noteMeta.setSize((long)contentStr.getBytes(StandardCharsets.UTF_8).length);
                noteDataMapper.insertSelective(noteData);
            } else {
                String fileId = fileStoreService.saveFile(file);
                mongoRollBackList.add(fileId);
                //先默认上传到mongo
                noteMeta.setStoreSite(NoteConstants.MONGO);
                noteMeta.setSiteId(fileId);
                //设置大小
                noteMeta.setSize(file.length());
                String tmpUrl = NoteConstants.getFileViewUrlSuffix(fileId);
//                String url = NoteConstants.BASE_URL+fileId;
                //store to t_note_file
                NoteFile noteFile = new NoteFile();
                noteFile.setFileId(fileId);
                noteFile.setName(noteMeta.getName());
                noteFile.setType(noteMeta.getType());
                noteFile.setSize(file.length());
                noteFile.setUserId(userId);
                noteFile.setUrl(tmpUrl);
                noteFile.setViewCount(0L);
                noteFile.setCreateTime(new Date());
                noteFile.setNoteRef(id);
                add(noteFile);
                //noteFileMapper.insertSelective(noteFile);
            }
        } else {
            File[] files = file.listFiles();
            for (File subFile : files) {
                if (subFile.getName().equals("images")) continue;
                generateTree(subFile, id, mongoRollBackList, syncStatisticList);
            }
        }
        //统计信息
        syncStatisticList.add(new LocalNoteSyncResult(id, isFile));
        noteMetaMapper.insertSelective(noteMeta);
    }

    private  String replaceImageUrls(String content, File srcFile, Long noteId, final List<String> mongoRollBackList) throws Exception{
        Pattern pattern = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String altText = matcher.group(1);
            String oldUrl = matcher.group(2);
            if (!oldUrl.startsWith("images")) {
                continue;
            }
            try {
                String srcFilePath = srcFile.getAbsolutePath();
                int ch = srcFilePath.lastIndexOf("\\");
                String targetPath = (srcFilePath.substring(0, ch)+"\\"+oldUrl).replace('/', '\\');
                File targetFile = new File(targetPath);
                String targetFileMongoId = fileStoreService.saveFile(targetFile);
                mongoRollBackList.add(targetFileMongoId);
                String vieUrlSuffix = NoteConstants.getFileViewUrlSuffix(targetFileMongoId);
                String viewUrl =NoteConstants.getBaseUrl()+vieUrlSuffix;
                //store to t_note_file
                NoteFile noteFile = new NoteFile();
                noteFile.setFileId(targetFileMongoId);
                String targetFileName = targetFile.getName();
                noteFile.setName(targetFileName);
                int dot = targetFileName.lastIndexOf('.');
                if (dot > 0) {
                    int len = targetFileName.length();
                    //获取文件后缀
                    String fileType = targetFileName.substring(dot + 1, len).toLowerCase();
                    if (fileType.length() > 10) {
                        noteFile.setType(FileTypeEnum.UNKNOWN.getValue());
                    } else {
                        noteFile.setType(fileType);
                    }
                } else {
                    noteFile.setType(FileTypeEnum.UNKNOWN.getValue());
                }
                noteFile.setSize(targetFile.length());
                noteFile.setUserId(LocalThreadUtils.getUserId());
                noteFile.setUrl(vieUrlSuffix);
                noteFile.setViewCount(0L);
                noteFile.setCreateTime(new Date());
                noteFile.setNoteRef(noteId);
                add(noteFile);
                //noteFileMapper.insertSelective(noteFile);
                matcher.appendReplacement(sb, "![" + altText + "](" + viewUrl + ")");
            } catch (Exception e) {
                matcher.appendReplacement(sb, "![" + altText + "](" + oldUrl + ")");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public JSONObject uploadFile(MultipartFile file, Long noteId) throws BusinessException {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        String fileId = null;
        try {
            fileId = fileStoreService.saveFile(file);
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();
            long fileSize = file.getSize();
            String fileViewUrlSuffix = NoteConstants.getFileViewUrlSuffix(fileId);
            NoteFile noteFile = new NoteFile();
            noteFile.setFileId(fileId);
            noteFile.setName(fileName);
            noteFile.setType(fileType);
            noteFile.setSize(fileSize);
            noteFile.setUserId(uid);
            noteFile.setUrl(fileViewUrlSuffix);
            noteFile.setNoteRef(noteId);
            noteFile.setCreateTime(new Date());
            add(noteFile);
            //noteFileMapper.insertSelective(noteFile);
            String url = NoteConstants.getBaseUrl()+fileViewUrlSuffix;
            JSONObject resJson = new JSONObject();
            resJson.put("url", url);
            resJson.put("alt", fileName);
            resJson.put("href", url);
            return resJson;
        } catch (Exception e) {
            if (fileId != null) {
                fileStoreService.delFile(fileId);
            }
            throw new BusinessException(CommonErrorCode.E_203004);
        }
    }

    /**
     * 根据文本内容插入文件
     * @param textContent
     * @param parentId
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public JSONObject uploadText(String textContent, Long parentId) {
        long id = idWorker.nextId();
        String fileName = "临时文本"+id;
        String fileType = FileTypeEnum.TXT.getValue();
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        NoteMeta note = new NoteMeta();
        note.setId(id);
        note.setParentId(parentId);
        note.setUserId(uid);
        note.setName(fileName);
        note.setIsFile("1");
        note.setType(fileType);
        note.setDel("0");
        note.setCreateTime(new Date());
        note.setSize((long)textContent.getBytes(StandardCharsets.UTF_8).length);
        note.setStoreSite(NoteConstants.MONGO);
        JSONObject resJson = new JSONObject();
        Path tempFile = null;
        String fileId = null;
        try {
            // 创建一个临时文件
            tempFile = Files.createTempFile(id+"", ".txt");
            Files.write(tempFile, textContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
            InputStream inputStream = new FileInputStream(tempFile.toFile());
            Map<String, Object> optionMap = new HashMap<>();
            optionMap.put(NoteConstants.OPTION_FILE_NAME, fileName);
            optionMap.put(NoteConstants.OPTION_FILE_TYPE, FileTypeEnum.TXT.getValue());
            optionMap.put(NoteConstants.OPTION_FILE_SIZE, textContent.getBytes(StandardCharsets.UTF_8).length);
            //bug20250721 遗漏新版本文件存储服务的方法实现，导致fileId为null
            fileId = fileStoreService.saveFile(inputStream, optionMap);
            note.setSiteId(fileId);
            //t_note_file
            String fileViewUrlSuffix = NoteConstants.getFileViewUrlSuffix(fileId);
            NoteFile noteFile = new NoteFile();
            noteFile.setFileId(fileId);
            noteFile.setName(fileName);
            noteFile.setType(fileType);
            noteFile.setSize((long) textContent.getBytes(StandardCharsets.UTF_8).length);
            noteFile.setUserId(uid);
            noteFile.setUrl(fileViewUrlSuffix);
            noteFile.setCreateTime(new Date());
            noteFile.setNoteRef(id);
            add(noteFile);
            //noteFileMapper.insertSelective(noteFile);
            String url = NoteConstants.getBaseUrl()+fileViewUrlSuffix;
            inputStream.close();
            resJson.put("url", url);
            resJson.put("id", fileId);
        } catch (Exception e) {
            if (fileId != null) {
                fileStoreService.delFile(fileId);
            }
            throw new RuntimeException(e);
        } finally {
            // 删除临时文件
            if (tempFile != null && Files.exists(tempFile)) {
                try {
                    Files.delete(tempFile);
                } catch (IOException e1) {
                    log.error("删除临时文件失败", e1);
                }
            }
        }
        noteMetaMapper.insertSelective(note);
        return resJson;
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 60)
    public void urlToPdf(String htmlUrl, Long parentId) {
        String toPdfServiceUrl = "http://localhost:9004/html2pdf/2pdf/fromUrl?url="+htmlUrl;
        ResponseEntity<byte[]> response = restTemplate.exchange(toPdfServiceUrl, HttpMethod.GET, null, byte[].class);
        byte[] body = response.getBody();
        if (body == null) {
            throw new BusinessException(CommonErrorCode.E_203006);
        }
        long id = idWorker.nextId();
        String fileName = "url转Pdf_"+id;
        String fileType = FileTypeEnum.PDF.getValue();
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        NoteMeta note = new NoteMeta();
        note.setId(id);
        note.setParentId(parentId);
        note.setUserId(uid);
        note.setName(fileName);
        note.setIsFile("1");
        note.setType(fileType);
        note.setDel("0");
        note.setCreateTime(new Date());
        note.setSize((long)body.length);
        note.setStoreSite(NoteConstants.MONGO);
        Path tempFile = null;
        String fileId = null;
        try {
            // 创建一个临时文件
            tempFile = Files.createTempFile(id+"", ".pdf");
            Files.write(tempFile, body, StandardOpenOption.WRITE);
            InputStream inputStream = new FileInputStream(tempFile.toFile());
            Map<String, Object> optionMap = new HashMap<>();
            optionMap.put(NoteConstants.OPTION_FILE_NAME, fileName);
            optionMap.put(NoteConstants.OPTION_FILE_TYPE, FileTypeEnum.PDF.getValue());
            optionMap.put(NoteConstants.OPTION_FILE_SIZE, body.length);
            fileId = fileStoreService.saveFile(inputStream, optionMap);
            note.setSiteId(fileId);
            //t_note_file
            String fileViewUrlSuffix = NoteConstants.getFileViewUrlSuffix(fileId);
            NoteFile noteFile = new NoteFile();
            noteFile.setFileId(fileId);
            noteFile.setName(fileName);
            noteFile.setType(fileType);
            noteFile.setSize((long) body.length);
            noteFile.setUserId(uid);
            noteFile.setUrl(fileViewUrlSuffix);
            noteFile.setCreateTime(new Date());
            noteFile.setNoteRef(id);
            add(noteFile);
            //noteFileMapper.insertSelective(noteFile);
            inputStream.close();
        } catch (Exception e) {
            //删除fileId
            if (fileId != null) {
                fileStoreService.delFile(fileId);
            }
            throw new RuntimeException(e);
        } finally {
            // 删除临时文件
            if (tempFile != null && Files.exists(tempFile)) {
                try {
                    Files.delete(tempFile);
                } catch (IOException e1) {
                    log.error("删除临时文件失败", e1);
                }
            }
        }
        noteMetaMapper.insertSelective(note);
    }

    @Override
    public void download(String id, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }
        NoteFile noteFile = findOne(id);
        log.debug("download: noteFile:{}", noteFile);
        //#20250828 pdf导出后，再次访问时无法访问，因为 noteFile等于null了，本质是这个pdf导出信息不再放入note_file表而是t_resource_file中去了
        if (noteFile != null) {
            NoteFile upCnt = new NoteFile();
            upCnt.setId(noteFile.getId());
            upCnt.setDownloadCount(noteFile.getDownloadCount()+1);
            noteFileMapper.updateByPrimaryKeySelective(upCnt);
        }
        AnyFile file = fileStoreService.loadFile(id);
        if (file != null) {
            resp.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(file.getFilename(), "UTF-8") + "\"");
            resp.addHeader("Content-Length", "" + file.getLength());
            resp.setContentType(file.getContentType());
            file.writeTo(resp.getOutputStream());
        }
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 60)
    public Long fetch(String url, String toType, Long parentId) {
        return noteFetchService.fetch(url, toType, parentId);
    }

    @Override
    public NoteFile add(NoteFile noteFile) {
        //bug20260203 若长度大于100（db定义) 报错
        String name = noteFile.getName();
        int nLen = name.length();
        if (nLen > 100) {
            name = name.substring(nLen-100, nLen);
            noteFile.setName(name);
        }
        String fileType = noteFile.getType();
        nLen = fileType.length();
        if (nLen > 30) {
            fileType = fileType.substring(nLen-30, nLen);
            noteFile.setType(fileType);
        }
        noteFileMapper.insertSelective(noteFile);
        return noteFile;
    }
}
