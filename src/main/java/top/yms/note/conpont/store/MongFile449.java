package top.yms.note.conpont.store;



import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import top.yms.note.conpont.AnyFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yangmingsen on 2024/4/13.
 */
public class MongFile449 implements AnyFile {

    GridFSFile gridFSFile;

    GridFSBucket gridFSBucket;


    public MongFile449(GridFSFile gridFSFile, GridFSBucket gridFSBucket) {
        this.gridFSFile = gridFSFile;
        this.gridFSBucket = gridFSBucket;
    }

    public long writeTo(OutputStream out) throws IOException {
        InputStream isp = getInputStream();
        int len;
        byte [] buf = new byte[1024];
        while ((len = isp.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        isp.close();
        out.close();

        return 0;
    }

    @Override
    public long getLength() {
        return gridFSFile.getLength();
    }

    @Override
    public String getContentType() {
        return gridFSFile.getMetadata().get("_contentType").toString();
    }

    @Override
    public String getFilename() {
        return gridFSFile.getFilename();
    }

    @Override
    public InputStream getInputStream() {
        return gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
    }
}
