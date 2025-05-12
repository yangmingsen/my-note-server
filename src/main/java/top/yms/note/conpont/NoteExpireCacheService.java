package top.yms.note.conpont;

public interface NoteExpireCacheService extends NoteCacheService{

    /**
     * 根据cacheId add cache
     * @param id cache id
     * @param data cache
     * @param second 过期时间 秒
     * @return cache data
     */
    Object add(String id, Object data, long second);

    /**
     * 根据cacheId update(add) cache
     * @param id cache id
     * @param data cache
     * @param second 过期时间 秒
     * @return cache data
     */
    Object update(String id, Object data, long second);
}
