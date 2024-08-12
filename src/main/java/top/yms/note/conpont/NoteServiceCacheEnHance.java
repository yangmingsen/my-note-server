package top.yms.note.conpont;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/4/13.
 */
@Component
public class NoteServiceCacheEnHance implements BeanPostProcessor, ApplicationListener {
    private final static Logger log = LoggerFactory.getLogger(NoteServiceCacheEnHance.class);

    private List<Object> list = new LinkedList<>();

    private boolean cached = false;

    @Value("${sys.enable-service-cache}")
    private boolean enableCache;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (enableCache) {
            if (beanName.equals("noteIndexService")) {
                log.info("stared modify {}", beanName);
                NoteIndexCacheCglibProxy noteCglibProxy = new NoteIndexCacheCglibProxy(bean);
                list.add(noteCglibProxy);
                return noteCglibProxy.getTargetProxy();
            }
        }

        return bean;
    }


    @Override
    public void onApplicationEvent(ApplicationEvent event) {
//        log.info("event={}", event.getSource());
        if (!cached && event.getSource() instanceof AnnotationConfigServletWebServerApplicationContext) {
            ApplicationContext context = (ApplicationContext) event.getSource();
            NoteCache cache = context.getBean(NoteCache.class);
            for(int i=0; i<list.size(); i++) {
                NoteIndexCacheCglibProxy proxy = (NoteIndexCacheCglibProxy) list.get(i);
                proxy.setNoteCache(cache);
            }
            cached = true;
        }

    }
}
