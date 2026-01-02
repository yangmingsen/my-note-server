package top.yms.note.conpont.queue.listener;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.conpont.queue.imsg.DelHashKeyMessage;
import top.yms.note.conpont.queue.imsg.DelKeyMessage;
import top.yms.note.conpont.queue.imsg.DelMulKeysMessage;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisKeyDeleteListener implements MessageListener {

    @Resource
    private NoteRedisCacheService cacheService;

    @Override
    public boolean support(IMessage message) {
        if (message instanceof DelKeyMessage) {
            return true;
        }
        if (message instanceof DelHashKeyMessage) {
            return true;
        }
        if (message instanceof DelMulKeysMessage) {
            return true;
        }
        return false;
    }

    @Override
    public void onMessage(IMessage message) {
        if (message instanceof DelKeyMessage) {
            String dKey = (String)message.getBody();
            cacheService.delete(dKey);
        } else if (message instanceof DelHashKeyMessage) {
            DelHashKeyMessage hashDel = (DelHashKeyMessage) message;
            cacheService.hDel(hashDel.getHash(), (String[])hashDel.getBody());
        } else if (message instanceof DelMulKeysMessage) {
            DelMulKeysMessage mulKeys = (DelMulKeysMessage) message;
            List<String> keyList = (List<String>)mulKeys.getBody();
            cacheService.del(keyList);
        }
    }
}
