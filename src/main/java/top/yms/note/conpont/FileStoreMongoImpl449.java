package top.yms.note.conpont;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.exception.BusinessException;
import top.yms.note.utils.MongoDB;

/**
 * Created by yangmingsen on 2024/4/13.
 */
@Component("mongoFileStore")
public class FileStoreMongoImpl449 implements FileStore{

    private final static Logger log = LoggerFactory.getLogger(FileStoreMongoImpl449.class);

    @Override
    public AnyFile loadFile(String id) throws BusinessException {
        try {
            log.info("loadFile id={}", id);
            return new MongFile449(MongoDB.loadFile(id));
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_203003);
        }

    }

    @Override
    public String saveFile(MultipartFile file, Object[] objs) {
        try {
            log.info("saveFile fileName={}", file.getOriginalFilename());
            return MongoDB.saveFile(file, null, (DBObject)objs[0]);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_203003);
        }
    }

    @Override
    public String saveFile(MultipartFile file) {
        return null;
    }

    @Override
    public boolean delFile(String id) {
        try {
            MongoDB.deleteFile(id);
        } catch (Exception e) {
            log.error("删除mongo文件失败: "+id, e);
            return false;
        }
        return true;
    }
}
