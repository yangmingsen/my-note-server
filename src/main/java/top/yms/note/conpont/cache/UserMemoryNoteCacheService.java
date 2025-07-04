package top.yms.note.conpont.cache;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.utils.LocalThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by yangmingsen on 2024/8/12.
 */
@Component(NoteConstants.userMemoryNoteCache)
public class UserMemoryNoteCacheService implements NoteCacheService {

    private final ConcurrentMap<String, Map<String, Object>> cacheMap = new ConcurrentHashMap<>();

    @Override
    public Object find(String id) {
        return cacheMap.get(getUserId()).get(id);
    }

    public Object find() {
        throw new RuntimeException("Not Support");
    }

    @Override
    public Object add(String id, Object data) {
        return cacheMap.get(getUserId()).put(id, data);
    }

    @Override
    public Object delete(String id) {
        throw new RuntimeException("Not Support");
    }

    @Override
    public Object update(String id, Object data) {
        throw new RuntimeException("Not Support");
    }

    private String getUserId() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        cacheMap.computeIfAbsent(uid.toString(), k -> new HashMap<>());
        return uid.toString();
    }

    @Override
    public void clear() {
        cacheMap.get(getUserId()).clear();
    }
}
