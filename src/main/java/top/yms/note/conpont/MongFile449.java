package top.yms.note.conpont;


import com.mongodb.gridfs.GridFSDBFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yangmingsen on 2024/4/13.
 */
public class MongFile449 extends AnyFile {

    private GridFSDBFile gridFSDBFile;

    public MongFile449(GridFSDBFile gridFSDBFile) {
        this.gridFSDBFile = gridFSDBFile;
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        return gridFSDBFile.writeTo(out);
    }

    @Override
    public long getLength() {
        return gridFSDBFile.getLength();
    }

    @Override
    public String getContentType() {
        return gridFSDBFile.getContentType();
    }

    @Override
    public String getFilename() {
        return gridFSDBFile.getFilename();
    }

    @Override
    public InputStream getInputStream() {
        return gridFSDBFile.getInputStream();
    }
}
