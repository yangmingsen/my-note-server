package top.yms.note.conpont.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;

import java.util.WeakHashMap;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component(NoteConstants.weakMemoryNoteCache)
public class WeakMemoryNoteCacheService implements NoteCacheService {
    private final static Logger log = LoggerFactory.getLogger(DefaultNoteCacheServiceImpl.class);

    private final WeakHashMap<String, Object> cacheMap = new WeakHashMap<>();

    @Override
    public Object find(String id) {
        log.debug("find id={}", id);
        return cacheMap.get(id);
    }

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
