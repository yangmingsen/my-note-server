package top.yms.note.conpont.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteFetchService;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.BusinessErrorCode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class NoteFetchServiceImpl implements NoteFetchService, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(NoteFetchServiceImpl.class);

    private final List<NoteFetch> componentList = new LinkedList<>();


    @Override
    public Long fetch(String url, String toType, Long parentId) {
        for (NoteFetch noteFetch : componentList) {
            if (noteFetch.supportFetch(toType)) {
                log.debug("find fetch component => {}", noteFetch);
                 return noteFetch.fetch(url, parentId);
            }
        }
        throw new BusinessException(BusinessErrorCode.E_204009);
    }

    public Long fetch(AbstractNoteFetch.FetchMeta fetchMeta, String toType) {
        for (NoteFetch noteFetch : componentList) {
            if (noteFetch.supportFetch(toType)) {
                log.debug("find fetch component => {}", noteFetch);
                return noteFetch.fetch(fetchMeta);
            }
        }
        throw new BusinessException(BusinessErrorCode.E_204009);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        componentList.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        context, NoteFetch.class, true, false).values());
        Collections.sort(componentList);
        log.info("获取到NoteFetch componentList: {}", componentList);
    }
}
