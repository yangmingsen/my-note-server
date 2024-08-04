package top.yms.note.service;

import com.alibaba.fastjson2.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.FileStore;
import top.yms.note.dao.NoteFileQuery;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.WangEditorUploadException;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.utils.LocalThreadUtils;
import top.yms.note.utils.MongoDB;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    @Qualifier("mongoFileStore")
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
            DBObject metaData = new BasicDBObject();
            metaData.put("type", Constants.MONGO_FILE_SITE);
//            String fileId = MongoDB.saveFile(file, null, metaData);
            String fileId = fileStore.saveFile(file, new Object[]{metaData} );
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

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
    public void addNote(MultipartFile file, NoteIndex note) throws Exception{
        DBObject metaData = new BasicDBObject();
        metaData.put("type", Constants.MONGO_FILE_SITE);
//        String fileId = MongoDB.saveFile(file, null, metaData);
        String fileId = fileStore.saveFile(file, new Object[]{metaData} );
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
        noteFile.setCreateTime(new Date());
        noteFileMapper.insertSelective(noteFile);
    }

}
