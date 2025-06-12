package top.yms.note.conpont;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 资源文件抽象接口
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
