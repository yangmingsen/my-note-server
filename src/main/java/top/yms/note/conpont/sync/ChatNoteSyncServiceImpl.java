package top.yms.note.conpont.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.ChatNoteSyncService;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ChatNoteSyncServiceImpl implements ChatNoteSyncService  {

    private static  final Logger log = LoggerFactory.getLogger(ChatNoteSyncServiceImpl.class);

    @Resource
    private  List<GptChatNoteSyncService> componentList;

    @Override
    public void sync() {
        for (GptChatNoteSyncService syncService : componentList) {
            if (syncService.support()) {
                try {
                    log.info("========Sync start [{}]============", syncService);
                    syncService.doSync();
                    log.info("========Sync end [{}]============", syncService);
                } catch (Exception e) {
                    log.error("ChatNoteSyncService Sync error", e);
                }
            }
        }
    }

}
