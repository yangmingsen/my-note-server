package top.yms.note.conpont.store.network;

import org.jetbrains.annotations.NotNull;
import top.yms.note.exception.ComponentException;
import top.yms.note.msgcd.ComponentErrorCode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NoteResourceInputStream extends InputStream {

    private static final int MAX_RESOURCE_SIZE = 1024*1024*1024*100; //100M

    private ResourceCacheInputStream resourceCacheInputStream;

    private final static class ResourceCacheInputStream extends ByteArrayInputStream {

        public ResourceCacheInputStream(byte[] buf) {
            super(buf);
        }

        public ResourceCacheInputStream(byte[] buf, int offset, int length) {
            super(buf, offset, length);
        }

        public void clear() {
            super.buf = null;
        }
    }

    private int cap = 0;

    public NoteResourceInputStream(InputStream inputStream) {
        int defaultSize = 1024*8;
        byte [] buf = new byte[defaultSize];
        int p = 0;
        try {
            int b = -1;
            while ((b = inputStream.read()) > -1) {
                buf[p] = (byte)b;
                p++;
                if (p == defaultSize) {
                    int oldLen = defaultSize;
                    defaultSize = (int)(defaultSize * 1.5f);
                    if (defaultSize > MAX_RESOURCE_SIZE) {
                        throw new ComponentException(ComponentErrorCode.E_204101);
                    }
                    byte [] buf2 = new byte[defaultSize];
                    System.arraycopy(buf, 0, buf2, 0, oldLen);
                    buf = buf2;
                }
            }
        } catch (Exception e) {
            throw new ComponentException(ComponentErrorCode.E_204100);
        }  finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                //ig
            }
        }
        System.out.println("defaultSize="+defaultSize);
        this.resourceCacheInputStream = new ResourceCacheInputStream(buf, 0, p);
        this.cap = p;
    }

    public int getCap() {
        return cap;
    }

    @Override
    public int read() throws IOException {
        return resourceCacheInputStream.read();
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        return resourceCacheInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return resourceCacheInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return resourceCacheInputStream.available();
    }

    @Override
    public void close() throws IOException {
        resourceCacheInputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        resourceCacheInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        resourceCacheInputStream.reset();
    }


    @Override
    public boolean markSupported() {
        return resourceCacheInputStream.markSupported();
    }

    public void clear() {
        resourceCacheInputStream.clear();
        resourceCacheInputStream = null;
    }
}
