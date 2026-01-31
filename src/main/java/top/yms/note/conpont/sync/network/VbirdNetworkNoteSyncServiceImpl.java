package top.yms.note.conpont.sync.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;

import java.util.HashMap;
import java.util.Map;

@Component
public class VbirdNetworkNoteSyncServiceImpl extends AbstractNetworkNoteSyncService implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(VbirdNetworkNoteSyncServiceImpl.class);

    private static final String secondLevelName = "鸟站Linux知识";

    private Long secondLevelId;

    private final static Map<String, String> transferMap = new HashMap<>();

    static {
        transferMap.put("linux_basic","基础篇");
        transferMap.put("linux_basic_train","训练教材");
        transferMap.put("linux_server","私服器篇");
        transferMap.put("enve","环工篇");
    }

    protected String getTransferName(String enName) {
        return transferMap.get(enName);
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
        //提取三级目录对应url的1级目录
        String thirdLevelName = extractLevelDirectory(param,1);
        thirdLevelName = transferName(thirdLevelName);
        //获取到三级目录id
        Long thirdLevelId = findOrCreate(thirdLevelName, parentId);
        //尝试四级目录 = url 2级目录
        String fourLevelName = extractLevelDirectory(param, 2);
        if (!fourLevelName.endsWith("空目录")) {//若是url存在2级目录则采用 4级目录
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
            if (url.contains("linux.vbird.org")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMessage(IMessage message) {
        NetworkNote networkNote = (NetworkNote)message.getBody();
        try {
            doDataSync(networkNote);
        } catch (Exception e) {
            log.error("Vbird onMessage error", e);
        }
    }
}
