package top.yms.note.conpont.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import top.yms.note.conpont.NoteCache;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by yangmingsen on 2024/8/19.
 */

public class NoteDataCacheCglibProxy implements MethodInterceptor, NoteCacheCglibProxy{
    private final static Logger log = LoggerFactory.getLogger(NoteDataCacheCglibProxy.class);

    private final Object target;

    private NoteCache noteCache;

    public NoteDataCacheCglibProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object objValue = null;
        try {
            // 反射调用目标类方法
            String methodName = method.getName();
             if (methodName.startsWith("find")) {
                 String cacheId = getCacheKey(proxy, method, args);
                 log.info("findCache: id={}", cacheId);
                 Object data = noteCache.find(cacheId);
                 if (data != null) {
                     return data;
                 }
                 objValue = method.invoke(target, args);
                 noteCache.add(cacheId, objValue);
                 return objValue;
             } else if (methodName.equals("addAndUpdate")) {
                noteCache.clear();
            }
            objValue = method.invoke(target, args);
            //log.info("返回值为：{}" , objValue);
        } catch (Exception e) {
            log.error("调用异常! " ,e);
            throw new Exception(e);
        }
        return objValue;
    }

    @Override
    public void setTarget(Object target) {

    }

    @Override
    public void setCache(NoteCache noteCache) {
        this.noteCache = noteCache;
    }

    @Override
    public Object getTargetProxy() {
        // Enhancer类是cglib中的一个字节码增强器，它可以方便的为你所要处理的类进行扩展
        Enhancer eh = new Enhancer();
        // 1.将目标对象所在的类作为Enhancer类的父类
        eh.setSuperclass(target.getClass());
        // 2.通过实现MethodInterceptor实现方法回调
        eh.setCallback(this);
        // 3. 创建代理实例
        return eh.create();
    }
}
