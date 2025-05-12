package top.yms.note.conpont.cache;

import top.yms.note.conpont.NoteCacheService;

/**
 * Created by yangmingsen on 2024/10/15.
 *
 * 自动检查当前key,是否过期，以及自动删除过期的key
 */
public class AutoClearMemoryNoteCacheService implements NoteCacheService {
    @Override
    public Object find(String id) {
        return null;
    }

    public Object find() {
        return null;
    }

    @Override
    public Object add(String id, Object data) {
        return null;
    }

    @Override
    public Object delete(String id) {
        return null;
    }

    @Override
    public Object update(String id, Object data) {
        return null;
    }
}
