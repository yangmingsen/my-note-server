package top.yms.note.conpont.sync.network;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;

@Component
public class RunoobNetworkNoteSyncServiceImpl extends AbstractNetworkNoteSyncService implements MessageListener {

    private static final String secondLevelName = "菜鸟教程";

    private Long secondLevelId;


    @Override
    Long getSecondLevelId(Long parentId) {
        if (secondLevelId != null) {
            return secondLevelId;
        }
        secondLevelId = findOrCreate(secondLevelName, parentId);
        return secondLevelId;
    }

    @Override
    Long getThirdLevelId(Long parentId, String param) {
        String thirdLevelName = extractFirstLevelDirectory(param);
        Long thirdLevelId = findOrCreate(thirdLevelName, parentId);
        return thirdLevelId;
    }

    @Override
    public boolean support(IMessage message) {
        return message instanceof NetworkNoteMessage;
    }

    @Override
    public void onMessage(IMessage message) {
        NetworkNote networkNote = (NetworkNote)message.getBody();
        doDataSync(networkNote);
    }
}
