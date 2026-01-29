package top.yms.note.conpont.sync.network;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;

import java.util.HashMap;
import java.util.Map;

@Component
public class WangDocNetworkNoteSyncServiceImpl extends AbstractNetworkNoteSyncService implements MessageListener {
    private static final String secondLevelName = "网道文档";

    private Long secondLevelId;

    private final static Map<String, String> transferMap = new HashMap<>();

    static {
        transferMap.put("html","HTML");
        transferMap.put("es6","ES6");
        transferMap.put("typescript","TypeScript");
        transferMap.put("webapi","WEB API");
        transferMap.put("clang","C语言");
        transferMap.put("bash","Bash教程");
        transferMap.put("ssh","SSH教程");
    }

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
        thirdLevelName = transferSecondCategroyName(thirdLevelName);
        Long thirdLevelId = findOrCreate(thirdLevelName, parentId);
        return thirdLevelId;
    }

    private String transferSecondCategroyName(String enName) {
        String toName = transferMap.get(enName);
        if (toName != null) {
            return toName;
        }
        return enName;
    }

    @Override
    public boolean support(IMessage message) {
        if (message instanceof NetworkNoteMessage) {
            NetworkNote networkNote = (NetworkNote)message.getBody();
            String url = networkNote.getUrl();
            if (url.contains("wangdoc")) {
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
