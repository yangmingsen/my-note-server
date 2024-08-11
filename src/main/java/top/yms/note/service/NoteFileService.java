package top.yms.note.service;

import com.alibaba.fastjson2.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.FileStore;
import top.yms.note.dao.NoteFileQuery;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.exception.WangEditorUploadException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Service
public class NoteFileService {

    private static Logger log = LoggerFactory.getLogger(NoteFileService.class);

    @Autowired
    private NoteFileMapper noteFileMapper;

    @Autowired
    private NoteIndexService noteIndexService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private NoteDataService noteDataService;

    @Autowired
    private NoteIndexMapper noteIndexMapper;

    @Autowired
    private FileStore fileStore;


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
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
    public JSONObject uploadFileForWer(MultipartFile file) throws WangEditorUploadException {
        JSONObject res = new JSONObject();

        Map<String, Object> reqInfo = LocalThreadUtils.get();
        long userId = (long)reqInfo.get(Constants.USER_ID);
        log.info("uploadFileForWer: userId={}", userId);

        try {
            String fileId = fileStore.saveFile(file);
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();
            long fileSize = file.getSize();
            String url = Constants.BASE_URL+fileId;

            NoteFile noteFile = new NoteFile();
            noteFile.setFileId(fileId);
            noteFile.setName(fileName);
            noteFile.setType(fileType);
            noteFile.setSize(fileSize);
            noteFile.setUserId(userId);
            noteFile.setUrl(url);
            noteFile.setCreateTime(new Date());
            noteFileMapper.insertSelective(noteFile);

            JSONObject data = new JSONObject();
            data.put("url", url);
            data.put("alt", fileName);
            data.put("href", url);
            res.put("data", data);
            res.put("errno", 0);
            return res;
        } catch (Exception e) {
            log.error("uploadFileForWer Error", e);
            throw new WangEditorUploadException(CommonErrorCode.E_203002);
        }

    }


    public NoteFile findOne(String fileId) {
        List<NoteFile> noteFiles = noteFileMapper.selectByExample(NoteFileQuery.Builder.build().fileId(fileId).get().example());
        if (noteFiles != null && noteFiles.size() >0) {
            return noteFiles.get(0);
        }
        return null;
    }


    private void handleMarkdown(MultipartFile file, NoteIndex note) throws Exception {
        long genId = idWorker.nextId();
        note.setStoreSite(Constants.MYSQL);
        note.setId(genId);
        //存储到t_note_index
        noteIndexService.add(note);

        StringBuilder sb = new StringBuilder();
        byte [] buf = new byte[1024];
        int len;
        InputStream inputStream = file.getInputStream();
        while ((len = inputStream.read(buf)) > 0) {
            sb.append(new String(buf, 0, len));
        }
        NoteData noteData = new NoteData();
        noteData.setId(genId);
        noteData.setUserId(note.getUserId());
        noteData.setContent(sb.toString());

        noteDataService.addAndUpdate(noteData);

    }


    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
    public void addNote(MultipartFile file, NoteIndex note) throws Exception{
        if (Constants.markdownSuffix.equals(note.getType())) {
            handleMarkdown(file, note);
            return;
        }
        String fileId = fileStore.saveFile(file);
        //先默认上传到mongo
        note.setStoreSite(Constants.MONGO);
        note.setSiteId(fileId);

        //存储到t_note_index
        noteIndexService.add(note);

        String url = Constants.BASE_URL+fileId;
        //store to t_note_file
        NoteFile noteFile = new NoteFile();
        noteFile.setFileId(fileId);
        noteFile.setName(note.getName());
        noteFile.setType(note.getType());
        noteFile.setSize(file.getSize());
        noteFile.setUserId(note.getUserId());
        noteFile.setUrl(url);
        noteFile.setViewCount(0L);
        noteFile.setCreateTime(new Date());
        noteFileMapper.insertSelective(noteFile);
    }


    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 20)
    public void generateTree(File file, Long parentId) throws Exception{
        if (file.getName().equals("images")) {
            return;
        }
        NoteIndex noteIndex = new NoteIndex();
        long id = idWorker.nextId();
        noteIndex.setId(id);
        noteIndex.setParentId(parentId);
        noteIndex.setUserId(1111L);
        boolean isFile = file.isFile();
        if (isFile) {
            noteIndex.setIsile("1");
        } else {
            noteIndex.setIsile("0");
        }
        String fileName = file.getName();
        noteIndex.setName(fileName);
        noteIndex.setCreateTime(new Date());
        if (isFile) {
            int dot = fileName.lastIndexOf('.');
            if (dot > 0) {
                int len = fileName.length();
                //获取文件后缀
                String fileType = fileName.substring(dot + 1, len).toLowerCase();
                if (fileType.length() > 10) {
                    noteIndex.setType(FileTypeEnum.UNKNOWN.getValue());
                } else {
                    noteIndex.setType(fileType);
                }

            } else {
                noteIndex.setType(FileTypeEnum.UNKNOWN.getValue());
            }

            if (Constants.markdownSuffix.equals(noteIndex.getType())) {
                noteIndex.setStoreSite(Constants.MYSQL);
                StringBuilder sb = new StringBuilder();
                byte [] buf = new byte[1024];
                int len;
                FileInputStream inputStream = new FileInputStream(file);
                while ((len = inputStream.read(buf)) > 0) {
                    sb.append(new String(buf, 0, len));
                }

                String contentStr = sb.toString();
                if (StringUtils.isNotBlank(contentStr))
                    contentStr = replaceImageUrls(contentStr, file);

                NoteData noteData = new NoteData();
                noteData.setId(id);
                noteData.setUserId(1111L);
                noteData.setContent(contentStr);
                noteData.setCreateTime(new Date());

                noteDataMapper.insertSelective(noteData);

            } else {
                String fileId = fileStore.saveFile(file);
                //先默认上传到mongo
                noteIndex.setStoreSite(Constants.MONGO);
                noteIndex.setSiteId(fileId);

                String url = Constants.BASE_URL+fileId;
                //store to t_note_file
                NoteFile noteFile = new NoteFile();
                noteFile.setFileId(fileId);
                noteFile.setName(noteIndex.getName());
                noteFile.setType(noteIndex.getType());
                noteFile.setSize(file.length());
                noteFile.setUserId(1111L);
                noteFile.setUrl(url);
                noteFile.setViewCount(0L);
                noteFile.setCreateTime(new Date());
                noteFileMapper.insertSelective(noteFile);

            }
        } else {
            File[] files = file.listFiles();
            for (File subFile : files) {
                if (subFile.getName().equals("images")) continue;
                generateTree(subFile, id);
            }
        }
        noteIndexMapper.insertSelective(noteIndex);
    }

    @Autowired
    private NoteDataMapper noteDataMapper;



    private  String replaceImageUrls(String content, File srcFile) throws Exception{
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
                String targetFileMongoId = fileStore.saveFile(targetFile);
                String viewUrl = Constants.BASE_URL+ targetFileMongoId;


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
                noteFile.setUserId(1111L);
                noteFile.setUrl(viewUrl);
                noteFile.setViewCount(0L);
                noteFile.setCreateTime(new Date());
                noteFileMapper.insertSelective(noteFile);

                String newUrl = viewUrl;
                matcher.appendReplacement(sb, "![" + altText + "](" + newUrl + ")");
            } catch (Exception e) {
                matcher.appendReplacement(sb, "![" + altText + "](" + oldUrl + ")");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }



}
