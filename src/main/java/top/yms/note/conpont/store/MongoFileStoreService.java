package top.yms.note.conpont.store;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.exception.NoteSystemException;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.msgcd.NoteSystemErrorCode;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Primary
@Component(NoteConstants.mongoFileStoreService)
public class MongoFileStoreService implements FileStoreService {

    private final static Logger log = LoggerFactory.getLogger(MongoFileStoreService.class);

    @Qualifier(NoteConstants.mongoFileStore449)
    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Qualifier(NoteConstants.bigFileGridFsBucket)
    @Resource
    private GridFSBucket newGridFSBucket;

    @Resource(name = NoteConstants.bigFileGridFsTemplate)
    private GridFsTemplate bigFileGridFsTemplate;

    //16M: 16M以内用document存储，超过用文件桶
    private static final long THRESHOLD_SIZE = 16 * 1024 * 1024;

    private String getNewId(String id) {
        return id.substring(2);
    }

    @Override
    public AnyFile loadFile(String id) {
        log.debug("new loadFile id={}",id);
        if (id.startsWith(NoteConstants.NEW_SMALL_FILE_PREFIX)) {
            //新版本小文件
            String newId = getNewId(id);
            SmallFileDocument doc = mongoTemplate.findById(newId, SmallFileDocument.class);
            return new NewMongoStoreFile(doc);
        } else if (id.startsWith(NoteConstants.NEW_BIG_FILE_PREFIX)) {
            //新版本大文件
            String newId = getNewId(id);
            GridFSFile gFS = bigFileGridFsTemplate.findOne(new Query(Criteria.where(NoteConstants._id).is(newId)));
            return new MongFile449(gFS, newGridFSBucket);
        } else {
            //旧 fs 库文件
            return fileStoreService.loadFile(id);
        }
    }

    @Override
    public String saveFile(MultipartFile file, Object[] objs) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public String saveFile(MultipartFile file) throws Exception {
        long size = file.getSize();
        if (size < THRESHOLD_SIZE) {
            SmallFileDocument doc = new SmallFileDocument();
            doc.setFilename(file.getOriginalFilename());
            doc.setContentType(file.getContentType());
            doc.setSize(size);
            doc.setUploadDate(new Date());
            doc.setData(file.getBytes());
            mongoTemplate.save(doc);
            return NoteConstants.NEW_SMALL_FILE_PREFIX+doc.getId();
        } else {
            ObjectId fileId = bigFileGridFsTemplate.store(
                    file.getInputStream(), file.getOriginalFilename(), file.getContentType());
            return NoteConstants.NEW_BIG_FILE_PREFIX+ fileId;
        }
    }

    @Override
    public String saveFile(InputStream inputStream, Map<String, Object> option) {
        String fileName = (String)option.get(NoteConstants.OPTION_FILE_NAME);
        String fileType = (String)option.get(NoteConstants.OPTION_FILE_TYPE);
        long fileSize = Long.parseLong(option.get(NoteConstants.OPTION_FILE_SIZE)+"");
        if (fileSize < THRESHOLD_SIZE) {
            String name = fileName+"."+fileType;
            SmallFileDocument doc = new SmallFileDocument();
            doc.setFilename(name);
            doc.setContentType(fileType);
            doc.setSize(fileSize);
            doc.setUploadDate(new Date());
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                byte [] temp  = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(temp)) != -1) {
                    buffer.write(temp, 0, bytesRead);
                }
            } catch (Exception e) {
               log.error("saveFile error", e);
               throw new NoteSystemException(NoteSystemErrorCode.E_400004);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("close stream error", e);
                }
            }
            doc.setData(buffer.toByteArray());
            mongoTemplate.save(doc);
            return NoteConstants.NEW_SMALL_FILE_PREFIX+doc.getId();
        } else {
            ObjectId fileId = bigFileGridFsTemplate.store(inputStream, fileName, fileType);
            try { inputStream.close();} catch (Exception ignored) {}
            return NoteConstants.NEW_BIG_FILE_PREFIX+ fileId;
        }
    }

    @Override
    public String saveFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        String fileName = file.getName();
        String fileType;
        long size = file.length();
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            int len = fileName.length();
            //获取文件后缀
            fileType = fileName.substring(dot + 1, len).toLowerCase();
        } else {
            fileType = FileTypeEnum.UNKNOWN.getValue();
        }
        Map<String, Object> optionMap = new HashMap<>();
        optionMap.put(NoteConstants.OPTION_FILE_NAME, fileName);
        optionMap.put(NoteConstants.OPTION_FILE_TYPE, fileType);
        optionMap.put(NoteConstants.OPTION_FILE_SIZE, size);
        return saveFile(fis, optionMap);
    }

    @Override
    public String saveFile(String localPath) throws Exception {
        return saveFile(new File(localPath));
    }

    @Override
    public boolean delFile(String id) {
        if (id.startsWith(NoteConstants.NEW_SMALL_FILE_PREFIX)) {
            String newId = getNewId(id);
            Query query = new Query(Criteria.where(NoteConstants._id).is(newId));
            mongoTemplate.remove(query, SmallFileDocument.class);
        } else if (id.startsWith(NoteConstants.NEW_BIG_FILE_PREFIX)) {
            String newId = getNewId(id);
            bigFileGridFsTemplate.delete(new Query(Criteria.where(NoteConstants._id).is(newId)));
        } else {
            fileStoreService.delFile(id);
        }
        return true;
    }

    @Override
    public String getStringContent(String id) {
        String resVal;
        if (id.startsWith(NoteConstants.NEW_SMALL_FILE_PREFIX)) {
            String newId = getNewId(id);
            SmallFileDocument doc = mongoTemplate.findById(newId, SmallFileDocument.class);
            if (doc == null) {
                throw new BusinessException(CommonErrorCode.E_200201);
            }
            resVal = new String(doc.getData(), StandardCharsets.UTF_8);
        } else if (id.startsWith(NoteConstants.NEW_BIG_FILE_PREFIX)) {
            //新版本大文件
            String newId = getNewId(id);
            GridFSFile gFS = bigFileGridFsTemplate.findOne(new Query(Criteria.where(NoteConstants._id).is(newId)));
            AnyFile anyFile = new MongFile449(gFS, newGridFSBucket);
            StringBuilder contentStr = new StringBuilder();
            try(InputStreamReader isr = new InputStreamReader(anyFile.getInputStream(), StandardCharsets.UTF_8)) {
                int bufLen = 1024;
                char [] cBuf = new char[bufLen];
                int rLen;
                while ((rLen = isr.read(cBuf)) > 0) {
                    contentStr.append(new String(cBuf, 0, rLen));
                }
            }catch (Exception e) {
                log.error("读取mongo文件内容出错", e);
                throw new RuntimeException(e);
            }
            resVal = contentStr.toString();
        } else {
            resVal = fileStoreService.getStringContent(id);
        }
        return resVal;
    }
}
