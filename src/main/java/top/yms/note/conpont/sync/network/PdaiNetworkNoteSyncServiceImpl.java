package top.yms.note.conpont.sync.network;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;

@Component
public class PdaiNetworkNoteSyncServiceImpl extends AbstractNetworkNoteSyncService  implements MessageListener {

    private static final String secondLevelName = "JAVA全栈知识体系";

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
        //提取三级目录对应url的二级目录
        String thirdLevelName = extractFirstLevelDirectory(param,2);
        //获取到三级目录id
        Long thirdLevelId = findOrCreate(thirdLevelName, parentId);
        //尝试四级目录 = url三级目录
        String fourLevelName = extractFirstLevelDirectory(param, 3);
        if (!fourLevelName.endsWith("空目录")) {//若是url存在3级目录则采用 4级目录
            Long fourLevelId = findOrCreate(fourLevelName, thirdLevelId);
            return fourLevelId;
        }
        return thirdLevelId;
    }

    @Override
    public boolean support(IMessage message) {
        if (message instanceof NetworkNoteMessage) {
            NetworkNote networkNote = (NetworkNote)message.getBody();
            String url = networkNote.getUrl();
            if (url.contains("pdai.tech")) {
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
