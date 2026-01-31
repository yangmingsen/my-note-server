package top.yms.note.conpont.cache;

import org.springframework.lang.Nullable;
import top.yms.note.conpont.NoteCacheService;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    /**
     * Remove given {@code values} from set at {@code key} and return the number of removed elements.
     *
     * @param key must not be {@literal null}.
     * @param values
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/srem">Redis Documentation: SREM</a>
     */
    Long sRem(String key, Object... values);

    /**
     * Get random element from set at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/srandmember">Redis Documentation: SRANDMEMBER</a>
     */
    Object sRandMember(String key);

    /**
     * Get size of set at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/scard">Redis Documentation: SCARD</a>
     */
    Long sCard(String key);

    /**
     * Remove and return {@code count} random members from set at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @param count number of random members to pop from the set.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/spop">Redis Documentation: SPOP</a>
     * @since 2.0
     */
    @Nullable
    List<Object> sPop(String key, long count);

    /**
     * Append {@code values} to {@code key}.
     *
     * @param key must not be {@literal null}.
     * @param values
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/rpush">Redis Documentation: RPUSH</a>
     */
    @Nullable
    Long rPush(String key, Object ... values);

    /**
     * Removes and returns first element in list stored at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return can be {@literal null}.
     * @see <a href="https://redis.io/commands/lpop">Redis Documentation: LPOP</a>
     */
    @Nullable
    Object lPop(String key);

    /**
     * Removes and returns first element from lists stored at {@code key} . <br>
     * <b>Blocks connection</b> until element available or {@code timeout} reached.
     *
     * @param key must not be {@literal null}.
     * @param timeout
     * @param unit must not be {@literal null}.
     * @return can be {@literal null}.
     * @see <a href="https://redis.io/commands/blpop">Redis Documentation: BLPOP</a>
     */
    @Nullable
    Object blPop(String key, long timeout, TimeUnit unit);

    /**
     * Get the size of list stored at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/llen">Redis Documentation: LLEN</a>
     */
    @Nullable
    Long lLen(String key);

    /**
     * Get element at {@code index} form list at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @param index
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/lindex">Redis Documentation: LINDEX</a>
     */
    @Nullable
    Object lIndex (String key, long index);

    /**
     * Get elements between {@code begin} and {@code end} from list at {@code key}.
     *
     * @param key must not be {@literal null}.
     * @param start
     * @param end
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/lrange">Redis Documentation: LRANGE</a>
     */
    @Nullable
    List<Object> lRange(String key, long start, long end);



}
