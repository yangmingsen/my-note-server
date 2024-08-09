package top.yms.note.conpont;

import org.springframework.web.multipart.MultipartFile;

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


    boolean delFile(String id);



}
