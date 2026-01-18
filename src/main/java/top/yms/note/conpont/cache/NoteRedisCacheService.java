package top.yms.note.conpont.cache;

import top.yms.note.conpont.NoteCacheService;

import java.util.List;
import java.util.Set;

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

    /**
     * Add given {@code values} to set at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @param values
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/sadd">Redis Documentation: SADD</a>
     */
    Long sAdd(String key, Object... values);

    /**
     * Check if set at {@code key} contains {@code value}.
     *
     * @param key must not be {@literal null}.
     * @param o
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/sismember">Redis Documentation: SISMEMBER</a>
     */
    Boolean sIsMember(String key, Object o);

    /**
     * del
     * @param key key
     */
    void del(String key);

    /**
     * Get all elements of set at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/smembers">Redis Documentation: SMEMBERS</a>
     */
    Set<Object> sMembers(String key);

}
