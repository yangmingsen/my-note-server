package top.yms.note.conpont.cache;

import top.yms.note.conpont.NoteCache;

import java.lang.reflect.Method;
import java.util.Arrays;

public interface NoteCacheCglibProxy {
    void setTarget(Object target);
    void setCache(NoteCache noteCache);
    Object getTargetProxy();

    default String getCacheKey(Object proxy, Method method, Object[] args) {
        return proxy.getClass().getSimpleName()+"_"+method.getName()+"_"+ Arrays.toString(args);
    }
}
