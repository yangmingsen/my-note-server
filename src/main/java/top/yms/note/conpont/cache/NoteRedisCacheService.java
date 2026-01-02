package top.yms.note.conpont.cache;

import top.yms.note.conpont.NoteCacheService;

import java.util.List;

public interface NoteRedisCacheService extends NoteCacheService {

    /**
     * set 命令 和 指定过期时间（秒）
     * @param key key
     * @param value value
     * @param time second
     */
    void set(String key, Object value, long time);

    /**
     * set 命令
     * @param key k
     * @param value v
     */
    void set(String key, Object value);

    /**
     * 获取v
     * @param key k
     * @return
     */
    Object get(String key);


    /**
     * hset 命令
     * @param hash hash
     * @param key key
     * @param value value
     * @return v
     */
    void hSet(String hash, String key, Object value);

    /**
     * hget命令
     * @param hash hash
     * @param key key
     * @return v
     */
    Object hGet(String hash, String key);


    /**
     * 多key删除
     * @param hash
     * @param keys
     */
    void hDel(String hash, String ...keys);

    /**
     * 多key删除
     * @param hash
     * @param keyList
     */
    void hDel(String hash, List<String> keyList);

    /**
     * 删除多个keys
     * @param keys
     */
    void del(String ...keys);

    /**
     * 删除多个keys
     * @param keyList
     */
    void del(List<String> keyList);

}
