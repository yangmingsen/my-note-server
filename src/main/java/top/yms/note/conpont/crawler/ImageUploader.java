package top.yms.note.conpont.crawler;


import top.yms.note.entity.NoteFile;

import java.io.InputStream;
import java.util.function.Consumer;

public interface ImageUploader {

    /**
     * @param inputStream 图片流
     * @param suffix 文件后缀，如 jpg/png
     * @return 新图片 URL
     */
    String upload(InputStream inputStream, String suffix, Consumer<NoteFile> consumer);

    /**
     * 异步上传
     * @param suffix
     * @param consumer
     * @return
     */
    String asyncUpload(String imgUrl, String suffix, Consumer<NoteFile> consumer);
}

