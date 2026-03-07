package top.yms.note.conpont.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.queue.ProducerService;
import top.yms.note.conpont.queue.imsg.DelHashKeyMessage;
import top.yms.note.conpont.queue.imsg.DelKeyMessage;
import top.yms.note.conpont.queue.imsg.DelMulKeysMessage;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Component(NoteConstants.noteRedisCacheServiceImpl)
public class NoteRedisCacheServiceImpl implements NoteRedisCacheService {

    private final static Logger log = LoggerFactory.getLogger(NoteRedisCacheServiceImpl.class);

    @Resource(name = NoteConstants.redisTemplate)
    private RedisTemplate<String, Object> redisTemplate;

    protected RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

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
            getRedisTemplate().delete(id);
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
            getRedisTemplate().opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } catch (Throwable th) {
            //log.error("set error", th);
        }
    }

    @Override
    public void set(String key, Object value) {
        try {
            getRedisTemplate().opsForValue().set(key, value);
        } catch (Throwable th) {
            //log.error("set error", th);
        }
    }

    @Override
    public Object get(String key) {
        try {
            return getRedisTemplate().opsForValue().get(key);
        } catch (Throwable th) {
            //log.error("get error", th);
        }
        return null;
    }

    @Override
    public void hSet(String hash, String key, Object value) {
        try {
            getRedisTemplate().opsForHash().put(hash, key, value);
        } catch (Exception e) {
            //log.error("hSet error", e);
        }
    }

    @Override
    public Object hGet(String hash, String key) {
        try {
            return getRedisTemplate().opsForHash().get(hash, key);
        } catch (Throwable th) {
            //log.error("hGet error", th);
        }
        return null;
    }

    @Override
    public Map<Object, Object> hGetAll(String hash) {
        return getRedisTemplate().opsForHash().entries(hash);
    }

    @Override
    public void hDel(String hash, String ... keys) {
        try {
            getRedisTemplate().opsForHash().delete(hash, keys);
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
            getRedisTemplate().delete(keyList);
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
            return getRedisTemplate().opsForSet().add(key, values);
        } catch (Throwable th) {
            //忽略
            return 0L;
        }
    }

    @Override
    public Boolean sIsMember(String key, Object o) {
        try {
            Boolean isMember = getRedisTemplate().opsForSet().isMember(key, o);
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
            return getRedisTemplate().opsForSet().members(key);
        } catch (Throwable th) {
            log.error("sMembers error: {}", th.getMessage());
            return null;
        }
    }

    @Override
    public Long sRem(String key, Object... values) {
        try {
            return getRedisTemplate().opsForSet().remove(key, values);
        } catch (Throwable th) {
            log.error("sRem error: {}", th.getMessage());
            return -1L;
        }
    }

    @Override
    public Object sRandMember(String key) {
        try {
            return getRedisTemplate().opsForSet().randomMember(key);
        } catch (Throwable th) {
            log.error("sRandMember error: {}", th.getMessage());
            return null;
        }
    }

    @Override
    public Long sCard(String key) {
        try {
            Long size =  getRedisTemplate().opsForSet().size(key);
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
            List<Object> objectList = getRedisTemplate().opsForSet().pop(key, count);
            if (objectList == null) {
                return Collections.emptyList();
            }
            return objectList;
        } catch (Throwable th) {
            log.error("sPop error: {}", th.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Long rPush(String key, Object... values) {
        Long res = getRedisTemplate().opsForList().rightPushAll(key, values);
        if (res == null) return 0L;
        return res;
    }

    @Override
    public Object lPop(String key) {
        Object res = getRedisTemplate().opsForList().leftPop(key);
        return res;
    }

    @Override
    public Object blPop(String key, long timeout, TimeUnit unit) {
        try {
            return getRedisTemplate().opsForList().leftPop(key, timeout, unit);
        } catch (org.springframework.dao.QueryTimeoutException qte) {
            log.info("blPop warning: {}", qte.getMessage());
        } catch (Exception e) {
            log.error("blPop error",e);
        }
        return null;
    }

    @Override
    public Long lLen(String key) {
        Long res = getRedisTemplate().opsForList().size(key);
        if (res == null) return 0L;
        return res;
    }

    @Override
    public Object lIndex(String key, long index) {
        return getRedisTemplate().opsForList().index(key, index);
    }

    @Override
    public List<Object> lRange(String key, long start, long end) {
        return Optional.ofNullable(getRedisTemplate().opsForList().range(key, start, end)).orElse(Collections.emptyList());
    }
}
