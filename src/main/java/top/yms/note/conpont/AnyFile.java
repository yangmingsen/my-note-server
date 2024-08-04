package top.yms.note.conpont;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by yangmingsen on 2024/4/13.
 */
public abstract class AnyFile {
    /**
     * 写到输出流
     * @param out
     * @return
     * @throws IOException
     */
    public abstract long writeTo(OutputStream out) throws IOException;

    public abstract long getLength();

    public abstract  String getContentType();

    public abstract String getFilename();
}
