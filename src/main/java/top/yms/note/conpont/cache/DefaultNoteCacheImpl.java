package top.yms.note.conpont.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/4/13.
 */
@Component(Constants.defaultNoteCache)
public class DefaultNoteCacheImpl implements NoteCache {

    private final static Logger log = LoggerFactory.getLogger(DefaultNoteCacheImpl.class);

    private final Map<String, Object> cacheMap = new ConcurrentHashMap<>();

    @Override
    public Object find(String id) {
//        log.info("find id={}", id);
        return cacheMap.get(id);
    }

    @Override
    public Object find() {
        return null;
    }

    @Override
    public Object add(String id, Object data) {
        Object o = cacheMap.get(id);
        if (o != null) {
            log.error("Cache id exist={}", id);
            throw new RuntimeException(id+" 存在");
        }
        return update(id, data);
    }

    @Override
    public Object delete(String id) {
        return cacheMap.remove(id);
    }

    @Override
    public Object update(String id, Object data) {
        return cacheMap.put(id, data);
    }
}
