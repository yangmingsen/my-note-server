package top.yms.note.conpont;

/**
 * 系统缓存服务
 */
public interface NoteCacheService {

    /**
     * 根据cache id 查询cache
     * @param id
     * @return
     */
    Object find(String id);

    /**
     * 根据id add cache data.
     * <p>注意若是id存在，则会添加失败</p>
     * @param id cache id
     * @param data cache data
     * @return cache data
     */
    Object add(String id, Object data);

    /**
     * 根据 cache id删除cache
     * @param id cache id
     * @return delete cache data
     */
    Object delete(String id);

    /**
     * 根据cache id更新data
     * @param id cacheId
     * @param data new cache data
     * @return old data if exist
     */
    Object update(String id, Object data);

    /**
     * 清空当前所有缓存，慎用
     */
    default void clear() {}
}
