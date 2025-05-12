package top.yms.note.utils;


import com.alibaba.ttl.TransmittableThreadLocal;
import top.yms.note.comm.NoteConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录线程信息
 *
 * @author yangmingsen
 *
 */
public class LocalThreadUtils {

    public static String getTmpVisitToken() {
        return (String)get().get(NoteConstants.TMP_VISIT_TOKEN);
    }

    public static Long getUserId() {
        return (Long)threadLocalValue.get().get(NoteConstants.USER_ID);
    }

    private static final ThreadLocal<Map<String, Object>> threadLocalValue = new TransmittableThreadLocal<>();
    //

    public static void set(Map<String, Object> values) {
        threadLocalValue.set(values);
    }

    /**
     * get设置后记得调用set进去
     * @return
     */
    public static Map<String, Object> get() {
        Map<String, Object> m = threadLocalValue.get();
        if (m == null)
            m = new HashMap<>();
        return m;
    }

    public static void remove() {
        threadLocalValue.get().clear();
        threadLocalValue.remove();
    }
}
