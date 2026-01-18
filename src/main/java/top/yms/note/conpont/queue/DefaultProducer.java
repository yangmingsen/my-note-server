package top.yms.note.conpont.queue;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DefaultProducer implements ProducerService{

    private final static Logger log = LoggerFactory.getLogger(DefaultProducer.class);

    @Resource
    private QueueClient queueClient;

    @Override
    public boolean send(IMessage message) {
        log.info("producer msg: {}", message);
        return queueClient.send(message);
    }
}
