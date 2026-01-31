package top.yms.note.conpont.store;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.note.NotePreview;
import top.yms.note.entity.NoteFile;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.msgcd.ComponentErrorCode;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by yangmingsen on 2024/4/13.
 */
@Component(NoteConstants.mongoFileStore449)
public class FileStoreServiceMongoImpl449 implements FileStoreService {

    private final static Logger log = LoggerFactory.getLogger(FileStoreServiceMongoImpl449.class);

    @Qualifier(NoteConstants.legacyGridFsTemplate)
    @Resource
    private GridFsTemplate gridFsTemplate;

    @Qualifier(NoteConstants.legacyGridFSBucket)
    @Resource
    private GridFSBucket gridFSBucket;

    @Resource
    private NotePreview notePreview;

    @Resource
    private NoteFileMapper noteFileMapper;


    @Override
    public AnyFile loadFile(String id) throws BusinessException {
        try {
            log.debug("loadFile id={}", id);
            GridFSFile gFS = gridFsTemplate.findOne(new Query(Criteria.where(NoteConstants._id).is(id)));
            if (gFS == null) {
                return null;
            }
            return new MongFile449(gFS, gridFSBucket);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_203003);
        }
    }


    @Override
    public String saveFile(MultipartFile file) throws Exception{
        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        return fileId.toString();
    }

    @Override
    public String saveFile(InputStream inputStream, Map<String, Object> option) {
        String fileName = (String)option.get(NoteConstants.OPTION_FILE_NAME);
        String fileType = (String)option.get(NoteConstants.OPTION_FILE_TYPE);
        ObjectId fileId = gridFsTemplate.store(inputStream, fileName, fileType);
        try { inputStream.close();} catch (Exception ignored) {}
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
        fis.close();
        return fileId.toString();
    }

    public String saveFile(String localPath) throws Exception{
        File file = new File(localPath);
        String fileId = saveFile(file);
        return fileId;
    }

    @Override
    public boolean delFile(String id) {
        try {
            gridFsTemplate.delete(new Query(Criteria.where(NoteConstants._id).is(id)));
            return true;
        } catch (Exception e) {
            log.error("删除mongo文件失败: id="+id, e);
            return false;
        }
    }

    @Override
    public String getStringContent(String id) {
        NoteFile noteFile = noteFileMapper.findOneByFileId(id);
        if (noteFile.getNoteRef() == 0L) {//这个地方兼容一下老版本时，fileId未与noteId关联的情况
            log.debug("警告: fileId={}, => noteId={}", id, noteFile.getNoteRef());
            return null;
        }
        if (!notePreview.canPreview(noteFile.getNoteRef())) {
            throw new BusinessException(ComponentErrorCode.E_204001);
        }
        AnyFile anyFile = loadFile(id);
        StringBuilder contentStr = new StringBuilder();
        try(InputStream is = anyFile.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
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
        return contentStr.toString();
    }
}
