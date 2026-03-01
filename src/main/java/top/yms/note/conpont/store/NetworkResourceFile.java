package top.yms.note.conpont.store;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.store.network.NoteResourceInputStream;
import top.yms.note.entity.NetworkResourceInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class NetworkResourceFile implements AnyFile {

    private final NoteResourceInputStream noteResourceInputStream;

    private final NetworkResourceInfo networkResourceInfo;

    public NetworkResourceFile(NoteResourceInputStream noteResourceInputStream, NetworkResourceInfo networkResourceInfo) {
        this.noteResourceInputStream = noteResourceInputStream;
        this.networkResourceInfo = networkResourceInfo;
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        int wl = 0;
        try (InputStream is = getInputStream()) {
            int len;
            byte [] buf = new byte[1024];
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
                wl+=len;
            }
        }
        return wl;
    }

    @Override
    public long getLength() {
        return noteResourceInputStream.getCap();
    }

    @Override
    public String getContentType() {
        String contentType = networkResourceInfo.getSuffix();
        String tName= networkResourceInfo.getFullName();
        Optional<MediaType> mediaTypeOptional = MediaTypeFactory.getMediaType(tName);
        MediaType mediaType = mediaTypeOptional.orElse(null);
        if (mediaType != null) {
            contentType = mediaType.toString();
        }
        return contentType;
    }

    @Override
    public String getFilename() {
        return networkResourceInfo.getFullName();
    }

    @Override
    public InputStream getInputStream() {
        return noteResourceInputStream;
    }
}
