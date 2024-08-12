package top.yms.note.conpont;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by yangmingsen on 2024/4/13.
 */

public class NoteIndexCacheCglibProxy implements MethodInterceptor {

    private final static Logger log = LoggerFactory.getLogger(NoteIndexCacheCglibProxy.class);

    private Object target;

    private NoteCache noteCache;

    public NoteIndexCacheCglibProxy(Object target) {
        this.target = target;
    }

    public void setNoteCache(NoteCache noteCache) {
        this.noteCache = noteCache;
    }

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


    /**
     * proxy：代理对象，CGLib动态生成的代理类实例
     * method：目标对象的方法，上文中实体类所调用的被代理的方法引用
     * args：目标对象方法的参数列表，参数值列表
     * methodProxy：代理对象的方法，生成的代理类对方法的代理引用
     */
    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        //log.info("proxy={}, method={}, args={}, methodProxy={}", proxy, method, args, methodProxy);
        Object objValue = null;
        try {
            // 反射调用目标类方法
            String methodName = method.getName();
            String cacheId = methodName;
            if (args != null && args.length > 0) {
                cacheId += Arrays.toString(args);
            }

            if (methodName.startsWith("find") || methodName.startsWith("get")) {
                log.info("findCache: id={}", cacheId);
                Object data = noteCache.find(cacheId);
                if (data != null) {
                    return data;
                }
                objValue = method.invoke(target, args);
                noteCache.add(cacheId, objValue);
                return objValue;
            } else if (methodName.startsWith("del") ||
                    methodName.startsWith("update") ||
                    methodName.startsWith("add") ||
                    methodName.startsWith("allDe")||
                    methodName.startsWith("allRe")
            ) {
                noteCache.clear();
            }
            objValue = method.invoke(target, args);
            //log.info("返回值为：{}" , objValue);
        } catch (Exception e) {
            log.error("调用异常! " ,e);
        } finally {
            //log.info("CGlibProxy调用结束...");
        }
        return objValue;
    }
}
