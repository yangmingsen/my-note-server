package top.yms.note.conpont.store;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.store.network.NoteResourceInputStream;
import top.yms.note.entity.AsyncFileSaveInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class NetworkResourceFile implements AnyFile {

    private NoteResourceInputStream noteResourceInputStream;

    private AsyncFileSaveInfo asyncFileSaveInfo;

    public NetworkResourceFile(NoteResourceInputStream noteResourceInputStream, AsyncFileSaveInfo asyncFileSaveInfo) {
        this.noteResourceInputStream = noteResourceInputStream;
        this.asyncFileSaveInfo = asyncFileSaveInfo;
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
        String contentType = asyncFileSaveInfo.getSuffix();
        String tName= asyncFileSaveInfo.getFullName();
        Optional<MediaType> mediaTypeOptional = MediaTypeFactory.getMediaType(tName);
        MediaType mediaType = mediaTypeOptional.orElse(null);
        if (mediaType != null) {
            contentType = mediaType.toString();
        }
        return contentType;
    }

    @Override
    public String getFilename() {
        return asyncFileSaveInfo.getTmpFileName();
    }

    @Override
    public InputStream getInputStream() {
        return noteResourceInputStream;
    }
}
