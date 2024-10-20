package top.yms.note.conpont;

import java.io.InputStream;

/**
 * tika服务, 为了支持各种类型的文件内容解析。
 * 使用初心为，lucene提供内容，以便建立索引。
 */
public interface NoteTikaService {

    /**
     * 从当前文件输入流中获取内容
     * @param inputStream
     * @return
     */
    String streamToString(InputStream inputStream);

}
