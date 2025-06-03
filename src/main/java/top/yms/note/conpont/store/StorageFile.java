package top.yms.note.conpont.store;

import top.yms.note.conpont.AnyFile;
import top.yms.storage.client.StorageClient;
import top.yms.storage.entity.FileMetaVo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StorageFile implements AnyFile {

    private String fileId;

    private StorageClient storageClient;

    private FileMetaVo fileMeta;

    public FileMetaVo getFileMeta() {
       if (fileMeta == null) {
           fileMeta = storageClient.getFileMetaInfo(fileId);
       }
       return fileMeta;
    }


    public StorageFile(String fileId, StorageClient storageClient) {
        this.fileId = fileId;
        this.storageClient = storageClient;
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        try (InputStream is = getInputStream()) {
            int len;
            byte [] buf = new byte[1024];
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            out.close();
            storageClient = null;
            fileMeta = null;
            fileId = null;
        }
        return 0;
    }

    @Override
    public long getLength() {
        return getFileMeta().getSize();
    }

    @Override
    public String getContentType() {
        return getFileMeta().getType();
    }

    @Override
    public String getFilename() {
        return getFileMeta().getName();
    }

    @Override
    public InputStream getInputStream() {
       return storageClient.getFileStream(this.fileId);
    }
}
