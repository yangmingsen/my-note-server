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
     * @param id 文件id
     * @return 任意文件
     */
    AnyFile loadFile(String id);

    /**
     * 文件上传
     * <p>直接使用MultipartFile类型上传</p>
     * @param file MultipartFile
     * @return 文件id
     * @throws Exception 上传异常时
     */
    String saveFile(MultipartFile file) throws Exception;

    /**
     * 支持带自定义参数文件上传
     * <p>场景：有时候需要指定文件的上传类型，名称等。 因为文件存储服务无法获取文件类型等</p>
     * @param inputStream 文件流
     * @param option 自定义参数
     * @return 文件id
     */
    String saveFile(InputStream inputStream, Map<String, Object> option);

    /**
     * 文件上传
     * @param file file
     * @return 文件id
     * @throws Exception 若是发生异常
     */
    String saveFile(File file) throws Exception;

    /**
     * 文件上传
     * <p>使用场景： 有时需要使用本地路径，但这个只用于个人本地机器场景</p>
     * @param localPath 本地路径
     * @return 文件id
     * @throws Exception 若是发生异常
     */
    String saveFile(String localPath) throws Exception;

    /**
     * 删除文件
     * @param id 文件id
     * @return true-del success or false del-fail
     */
    boolean delFile(String id);

    /**
     * 从文件系统获取该文件的文本内容，注意目标必须是文本文件，否则将出现不可预览异常
     * <p>使用场景：为lucene服务，获取文本内容</p>
     * @param id fileId, 不是noteid
     * @return 获取文件内容，目前只会是文本
     */
    String getStringContent(String id);

}
