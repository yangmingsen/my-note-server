package top.yms.note.conpont.sync.network;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;

import java.util.HashMap;
import java.util.Map;

@Component
public class OiWikiNetworkSyncServiceImpl extends AbstractNetworkNoteSyncService   implements MessageListener {

    private static final String secondLevelName = "OI-WIKI";

    private Long secondLevelId;

    private final static Map<String, String> transferMap = new HashMap<>();

    static {
        transferMap.put("tools","工具软件");
        transferMap.put("lang","语言基础");
        transferMap.put("basic","算法基础");
        transferMap.put("search","搜索");
        transferMap.put("dp","动态规划");
        transferMap.put("string","字符串");
        transferMap.put("math","数学");
        transferMap.put("ds","数据结构");
        transferMap.put("graph","图论");
        transferMap.put("geometry","计算几何");
        transferMap.put("misc","杂项");
        transferMap.put("topic","专题");
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
        thirdLevelName = transferName(thirdLevelName);
        Long thirdLevelId = findOrCreate(thirdLevelName, parentId);
        return thirdLevelId;
    }

    private String transferName(String enName) {
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
            if (url.contains("oi-wiki")) {
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
