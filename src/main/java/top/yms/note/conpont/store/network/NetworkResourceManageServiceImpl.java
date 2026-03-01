package top.yms.note.conpont.store.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.http.NoteHttpRequestService;
import top.yms.note.exception.ComponentException;
import top.yms.note.msgcd.ComponentErrorCode;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@Component
public class NetworkResourceManageServiceImpl implements NetworkResourceManageService{

    private static final Logger log = LoggerFactory.getLogger(NetworkResourceManageServiceImpl.class);

    @Resource
    private NoteHttpRequestService noteHttpRequestService;

    @Override
    public NoteResourceInputStream getNoteResourceInputStream(String resourceUrl) {
        return new NoteResourceInputStream(getInputStream(resourceUrl));
    }

    @Override
    public InputStream getInputStream(String resourceUrl) {
        try {
            return noteHttpRequestService.openImageStream(resourceUrl);
        } catch (IOException e) {
            log.error("getInputStream error", e);
            throw new ComponentException(ComponentErrorCode.E_204102);
        }
    }
}
