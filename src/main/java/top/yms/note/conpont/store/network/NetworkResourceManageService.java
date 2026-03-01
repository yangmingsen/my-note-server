package top.yms.note.conpont.store.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public interface NetworkResourceManageService {

    NoteResourceInputStream getNoteResourceInputStream(String resourceUrl);

    InputStream getInputStream(String resourceUrl);

}
