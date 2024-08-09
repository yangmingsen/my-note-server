package top.yms.note.conpont;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yangmingsen on 2024/4/13.
 */
public  interface AnyFile {
    /**
     * 写到输出流
     * @param out
     * @return
     * @throws IOException
     */
      long writeTo(OutputStream out) throws IOException;

      long getLength();

      String getContentType();

      String getFilename();

      InputStream getInputStream();
}
