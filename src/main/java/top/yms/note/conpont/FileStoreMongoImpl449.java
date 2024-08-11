package top.yms.note.conpont;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.exception.BusinessException;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by yangmingsen on 2024/4/13.
 */
@Component("mongoFileStore449")
public class FileStoreMongoImpl449 implements FileStore{

    private final static Logger log = LoggerFactory.getLogger(FileStoreMongoImpl449.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;


    @Override
    public AnyFile loadFile(String id) throws BusinessException {
        try {
            log.info("loadFile id={}", id);
            GridFSFile gFS = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
            return new MongFile449(gFS, gridFSBucket);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_203003);
        }
    }

    @Override
    public String saveFile(MultipartFile file, Object[] objs) {
        throw new RuntimeException("Method Not Implement");
    }

    @Override
    public String saveFile(MultipartFile file) throws Exception{
        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        return fileId.toString();
    }

    @Override
    public String saveFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);

        String fileName = file.getName();
        String fileType;
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            int len = fileName.length();
            //获取文件后缀
            fileType = fileName.substring(dot + 1, len).toLowerCase();
        } else {
            fileType = FileTypeEnum.UNKNOWN.getValue();
        }

        ObjectId fileId = gridFsTemplate.store(fis, file.getName(), fileType);
        return fileId.toString();
    }

    @Override
    public boolean delFile(String id) {
        try {
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
            return true;
        } catch (Exception e) {
            log.error("删除mongo文件失败: id="+id, e);
            return false;
        }
    }
}
