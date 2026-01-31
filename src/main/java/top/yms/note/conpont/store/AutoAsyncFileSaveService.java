package top.yms.note.conpont.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.http.NoteHttpRequestService;
import top.yms.note.conpont.task.NoteTask;
import top.yms.note.service.FileStoreRelationService;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class AutoAsyncFileSaveService implements NoteTask, InitializingBean {

    private static  final Logger log = LoggerFactory.getLogger(AutoAsyncFileSaveService.class);

    @Resource
    private NoteRedisCacheService cacheService;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private NoteHttpRequestService noteHttpRequestService;

    private Thread thread = new Thread(this);

    @Override
    public void run() {
        while (true) {
            try {
                Object oV = cacheService.blPop(NoteCacheKey.ASYNC_UPLOAD_FILE_LIST, 15, TimeUnit.SECONDS);
                if (oV == null) {
                    continue;
                }
                AsyncFileSaveDto fileSaveDto = (AsyncFileSaveDto) oV;
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put(NoteConstants.OPTION_FILE_NAME, fileSaveDto.getTmpFileName());
                optionMap.put(NoteConstants.OPTION_FILE_TYPE, fileSaveDto.getSuffix());
                optionMap.put(NoteConstants.OPTION_FILE_ID, fileSaveDto.getNoteFileId());
                String fetchUrl = fileSaveDto.getFetchUrl();
                try (InputStream inputStream = noteHttpRequestService.openImageStream(fetchUrl)) {
                    fileStoreService.saveFile(inputStream, optionMap);
                    log.info("fetch image success. url={}", fetchUrl);
                } catch (Exception e) {
                    log.error("auto async fetch image error: {}", e.getMessage());
                    //进入失败队列
                    cacheService.rPush(NoteCacheKey.ASYNC_UPLOAD_FILE_FAIL_LIST, fileSaveDto);
                }
            } catch (Exception e) {
                log.error("AutoAsyncFileSaveService error", e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        thread.start();
    }
}
