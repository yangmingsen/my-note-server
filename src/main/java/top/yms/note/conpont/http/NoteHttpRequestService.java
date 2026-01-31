package top.yms.note.conpont.http;

import java.io.IOException;
import java.io.InputStream;

public interface NoteHttpRequestService {

    InputStream openImageStream(String imgUrl) throws IOException;

}
