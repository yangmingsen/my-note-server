package top.yms.note.conpont.cache;

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
import top.yms.note.conpont.NoteCacheService;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/4/13.
 */
@Component
public class NoteServiceCacheEnHance implements BeanPostProcessor, ApplicationListener {
    private final static Logger log = LoggerFactory.getLogger(NoteServiceCacheEnHance.class);

    private List<NoteCacheCglibProxy> listCache = new LinkedList<>();

    private boolean cached = false;

    @Value("${sys.enable-service-cache}")
    private boolean enableCache;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (enableCache) {
            if (beanName.equals("noteIndexService")) {
                log.info("started NoteIndexCacheCglibProxy");
                NoteCacheCglibProxy noteCglibProxy = new NoteIndexCacheCglibProxy(bean);
                listCache.add(noteCglibProxy);
                return noteCglibProxy.getTargetProxy();
            } else if  (beanName.equals("noteFileService")) {
                log.info("started NoteFileCacheCglibProxy");
                NoteCacheCglibProxy noteCglibProxy = new NoteFileCacheCglibProxy(bean);
                listCache.add(noteCglibProxy);
                return noteCglibProxy.getTargetProxy();
            } else if (beanName.equals("noteDataService")) {
                log.info("started NoteDataCacheCglibProxy");
                NoteCacheCglibProxy noteCacheCglibProxy = new NoteDataCacheCglibProxy(bean);
                listCache.add(noteCacheCglibProxy);
                return noteCacheCglibProxy.getTargetProxy();
            }
        }

        return bean;
    }


    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!cached && event.getSource() instanceof AnnotationConfigServletWebServerApplicationContext) {
            ApplicationContext context = (ApplicationContext) event.getSource();
            NoteCacheService cache = context.getBean(NoteCacheService.class);
            for(int i = 0; i< listCache.size(); i++) {
                NoteCacheCglibProxy proxy = listCache.get(i);
                proxy.setCache(cache);
            }
            cached = true;
        }

    }
}
