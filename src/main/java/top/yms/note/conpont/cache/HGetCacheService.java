package top.yms.note.conpont.cache;

public interface HGetCacheService {
    Object hGet(String k1, String k2);

    Object hPut(String k1, String k2, Object obj);
}
