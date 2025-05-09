package top.yms.note.conpont.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import top.yms.note.conpont.NoteCacheService;

import java.lang.reflect.Method;

public class NoteServiceCacheCglibProxy implements MethodInterceptor, NoteCacheCglibProxy{
    private final static Logger log = LoggerFactory.getLogger(NoteServiceCacheCglibProxy.class);
    private Object target;
    private NoteCacheService noteCacheService;

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return null;
    }

    @Override
    public void setTarget(Object target) {

    }

    @Override
    public void setCache(NoteCacheService noteCacheService) {

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
