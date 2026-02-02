package top.yms.note.conpont.sync.network;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;

@Component
public class ConcretePageNetworkNoteSyncServiceImpl extends AbstractNetworkNoteSyncService implements MessageListener {

    private static final String secondLevelName = "ConcretePage技术知识";

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
        String thirdLevelName = extractLevelDirectory(param);
        Long thirdLevelId = findOrCreate(thirdLevelName, parentId);
        return thirdLevelId;
    }

    @Override
    public boolean support(IMessage message) {
        if (message instanceof NetworkNoteMessage) {
            NetworkNote networkNote = (NetworkNote)message.getBody();
            String url = networkNote.getUrl();
            if (url.contains("www.concretepage.com")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMessage(IMessage message) {
        NetworkNote networkNote = (NetworkNote)message.getBody();
        doDataSync(networkNote);
    }
}
