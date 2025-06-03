package top.yms.note.conpont;

import org.springframework.web.multipart.MultipartFile;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * 系统文件存储服务
 */
public interface FileStoreService {

    /**
     * 根据id取File
     * @param id
     * @return
     */
    AnyFile loadFile(String id);

    /**
     * 存储文件，返回id
     * 已过期，请使用saveFile(file)
     * @param file
     * @return
     */
    @Deprecated
    default String saveFile(MultipartFile file, Object [] objs) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }


    String saveFile(MultipartFile file) throws Exception;

    default String saveFile(InputStream inputStream, Map<String, Object> option) {
        return null;
    }

    String saveFile(File file) throws Exception;

    String saveFile(String localPath) throws Exception;



    boolean delFile(String id);

    /**
     * 从文件系统获取该文件的文本内容，注意目标必须是文本文件，否则将出现不可预览异常
     * @param id fileId, 不是noteid
     * @return
     */
    default String getStringContent(String id) {return null;}

}
