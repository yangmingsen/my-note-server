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
    private RedisTemplate<String, Object> redisTemplate;

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

    @Override
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Throwable th) {
            //忽略
            return 0L;
        }
    }

    @Override
    public Boolean sIsMember(String key, Object o) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(key, o);
            if (isMember == null) {
                return false;
            }
            return isMember;
        } catch (Throwable th) {
            //忽略
            return false;
        }
    }

    @Override
    public void del(String key) {
        delete(key);
    }

    @Override
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Throwable th) {
            log.error("sMembers error: {}", th.getMessage());
            return null;
        }
    }

    @Override
    public Long sRem(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Throwable th) {
            log.error("sRem error: {}", th.getMessage());
            return -1L;
        }
    }

    @Override
    public Object sRandMember(String key) {
        try {
            return redisTemplate.opsForSet().randomMember(key);
        } catch (Throwable th) {
            log.error("sRandMember error: {}", th.getMessage());
            return null;
        }
    }

    @Override
    public Long sCard(String key) {
        try {
            Long size =  redisTemplate.opsForSet().size(key);
            if (size == null) {
                return 0L;
            }
            return size;
        } catch (Throwable th) {
            log.error("sCard error: {}", th.getMessage());
            return 0L;
        }
    }

    @Override
    public List<Object> sPop(String key, long count) {
        try {
            List<Object> objectList = redisTemplate.opsForSet().pop(key, count);
            if (objectList == null) {
                return Collections.emptyList();
            }
            return objectList;
        } catch (Throwable th) {
            log.error("sPop error: {}", th.getMessage());
            return Collections.emptyList();
        }
    }
}
