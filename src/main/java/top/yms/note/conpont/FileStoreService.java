package top.yms.note.conpont;

import org.springframework.web.multipart.MultipartFile;

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
    String saveFile(MultipartFile file, Object [] objs);


    String saveFile(MultipartFile file) throws Exception;

    default String saveFile(InputStream inputStream, Map<String, Object> option) {
        return null;
    }

    default String saveFile(File file) throws Exception {
        return null;
    }



    boolean delFile(String id);

    /**
     * 从文件系统获取该文件的文本内容，注意目标必须是文本文件，否则将出现不可预览异常
     * @param id fileId, 不是noteid
     * @return
     */
    default String getStringContent(String id) {return null;}

}
