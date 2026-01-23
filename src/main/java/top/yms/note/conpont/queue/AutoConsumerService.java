package top.yms.note.conpont.queue;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.task.NoteTask;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class AutoConsumerService implements ConsumerService, ApplicationListener<ApplicationStartedEvent> {

    private final static Logger log = LoggerFactory.getLogger(AutoConsumerService.class);

    @Resource
    private QueueClient queueClient;

    private List<MessageListener> messageListeners = new LinkedList<>();

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        messageListeners.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        context, MessageListener.class, true, false).values());
        log.info("messageListeners={}", messageListeners);
    }

    @Override
    public void consumer(IMessage iMessage) {
        AutoConsumerService.log.info("consumer msg: {}", iMessage);
        for (MessageListener messageListener : messageListeners) {
            if (messageListener.support(iMessage)) {
                try {
                    messageListener.onMessage(iMessage);
                } catch (Throwable th1) {
                    AutoConsumerService.log.error("onMessage error: ", th1);
                    //reSend
                    AutoConsumerService.log.info("ReSend msg: {}", iMessage);
                    queueClient.send(iMessage);
                }
            }
        }
    }
}
