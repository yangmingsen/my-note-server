package top.yms.note.utils;


import java.util.HashMap;
import java.util.Map;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 记录线程信息
 *
 * @author yangmingsen
 *
 */
public class LocalThreadUtils {

    private static final ThreadLocal<Map<String, Object>> threadLocalValue = new TransmittableThreadLocal<>();
    //

    public static final void set(Map<String, Object> values) {
        threadLocalValue.set(values);
    }

    public static final Map<String, Object> get() {
        Map<String, Object> m = threadLocalValue.get();
        if (m == null)
            m = new HashMap<>();
        return m;
    }

    public static final void remove() {
        threadLocalValue.get().clear();
        threadLocalValue.remove();
    }
}
