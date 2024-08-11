package top.yms.note.conpont;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileStore {

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

    default String saveFile(File file) throws Exception {
        return null;
    }



    boolean delFile(String id);



}
