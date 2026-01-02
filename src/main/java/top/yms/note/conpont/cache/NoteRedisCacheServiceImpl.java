package top.yms.note.conpont.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.queue.ProducerService;
import top.yms.note.conpont.queue.imsg.DelHashKeyMessage;
import top.yms.note.conpont.queue.imsg.DelKeyMessage;
import top.yms.note.conpont.queue.imsg.DelMulKeysMessage;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class NoteRedisCacheServiceImpl implements NoteRedisCacheService {

    private final static Logger log = LoggerFactory.getLogger(NoteRedisCacheServiceImpl.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ProducerService producerService;

    @Override
    public Object find(String id) {
        return get(id);
    }

    @Override
    public Object add(String id, Object data) {
        Object oldV = get(id);
        if (oldV != null) {
            throw new RuntimeException("add cache error, because old value exist.");
        }
        set(id, data);
        return data;
    }

    @Override
    public Object delete(String id) {
        try {
            redisTemplate.delete(id);
        } catch (Throwable th) {
            //log.error("delete key error", th);
            //resend msg
            DelKeyMessage delMsg = new DelKeyMessage();
            delMsg.setBody(id);
            producerService.send(delMsg);
        }
        return null;
    }

    @Override
    public Object update(String id, Object data) {
        Object oldV = get(id);
        set(id, data);
        return oldV;
    }

    @Override
    public void set(String key, Object value, long time) {
        try {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } catch (Throwable th) {
            //log.error("set error", th);
        }
    }

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Throwable th) {
            //log.error("set error", th);
        }
    }

    @Override
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Throwable th) {
            //log.error("get error", th);
        }
        return null;
    }

    @Override
    public void hSet(String hash, String key, Object value) {
        try {
            redisTemplate.opsForHash().put(hash, key, value);
        } catch (Exception e) {
            //log.error("hSet error", e);
        }
    }

    @Override
    public Object hGet(String hash, String key) {
        try {
            return redisTemplate.opsForHash().get(hash, key);
        } catch (Throwable th) {
            //log.error("hGet error", th);
        }
        return null;
    }


    @Override
    public void hDel(String hash, String ... keys) {
        try {
            redisTemplate.opsForHash().delete(hash, keys);
        } catch (Throwable th) {
            //log.error("hDel error", th);
            //resend msg
            DelHashKeyMessage delMsg = new DelHashKeyMessage();
            delMsg.setHash(hash);
            delMsg.setBody(keys);
            producerService.send(delMsg);
        }
    }

    @Override
    public void hDel(String hash, List<String> keyList) {
        String[] array2 = keyList.toArray(new String[keyList.size()]);
        hDel(hash, array2);
    }

    @Override
    public void del(String ...keys) {
        List<String> keyList = Arrays.asList(keys);
        del(keyList);
    }

    @Override
    public void del(List<String> keyList) {
        try {
            redisTemplate.delete(keyList);
        } catch (Throwable th) {
            //log.error("del error", th);
            DelMulKeysMessage delMsg = new DelMulKeysMessage();
            delMsg.setBody(keyList);
            producerService.send(delMsg);
        }
    }
}
