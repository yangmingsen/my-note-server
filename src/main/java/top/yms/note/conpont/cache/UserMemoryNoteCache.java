package top.yms.note.conpont.cache;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteCache;
import top.yms.note.utils.LocalThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by yangmingsen on 2024/8/12.
 */
@Primary
@Component(Constants.userMemoryNoteCache)
public class UserMemoryNoteCache implements NoteCache {

    private final ConcurrentMap<String, Map<String, Object>> cacheMap = new ConcurrentHashMap<>();

    @Override
    public Object find(String id) {
        return cacheMap.get(getUserId()).get(id);
    }

    @Override
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
        Long uid = (Long) LocalThreadUtils.get().get(Constants.USER_ID);
        cacheMap.computeIfAbsent(uid.toString(), k -> new HashMap<>());
        return uid.toString();
    }

    @Override
    public void clear() {
        cacheMap.get(getUserId()).clear();
    }
}
