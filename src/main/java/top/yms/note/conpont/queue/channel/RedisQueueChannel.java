package top.yms.note.conpont.queue.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.queue.IMessage;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component(NoteConstants.redisQueueChannel)
public class RedisQueueChannel extends AbstractQueueChannel{

    private final static Logger log = LoggerFactory.getLogger(RedisQueueChannel.class);

    private final static String HIGH_QUEUE_NAME = NoteCacheKey.QUEUE_HIGH_NAME_LIST;

    private final static String MEDIUM_QUEUE_NAME = NoteCacheKey.QUEUE_MEDIUM_NAME_LIST;

    private final static String LOW_QUEUE_NAME = NoteCacheKey.QUEUE_LOW_NAME_LIST;

    @Resource(name = NoteConstants.noteRedisCacheServiceImpl)
    private NoteRedisCacheService cacheService;


    @Override
    boolean offer2HighQueue(IMessage iMessage) {
        Long aLong = cacheService.rPush(HIGH_QUEUE_NAME, iMessage);
        return aLong > 0L;
    }

    @Override
    boolean offer2MediumQueue(IMessage iMessage) {
        Long aLong = cacheService.rPush(MEDIUM_QUEUE_NAME, iMessage);
        return aLong > 0L;
    }

    @Override
    boolean offer2LowQueue(IMessage iMessage) {
        Long aLong = cacheService.rPush(LOW_QUEUE_NAME, iMessage);
        return aLong > 0L;
    }

    @Override
    public IMessage takeFromHigh() throws Exception {
        while (true) {
            Object oV = cacheService.blPop(HIGH_QUEUE_NAME, 15, TimeUnit.SECONDS);
            if (oV == null) {
                continue;
            }
            return (IMessage)oV;
        }
    }

    @Override
    public IMessage takeFromMedium() throws Exception {
        while (true) {
            Object oV = cacheService.blPop(MEDIUM_QUEUE_NAME, 15, TimeUnit.SECONDS);
            if (oV == null) {
                continue;
            }
            return (IMessage)oV;
        }
    }

    @Override
    public IMessage takeFromLow() throws Exception {
        while (true) {
            Object oV = cacheService.blPop(LOW_QUEUE_NAME, 15, TimeUnit.SECONDS);
            if (oV == null) {
                continue;
            }
            return (IMessage)oV;
        }
    }

    @Override
    public IMessage pollFromMedium(long timeout, TimeUnit unit) {
        Object o = cacheService.blPop(MEDIUM_QUEUE_NAME, timeout, unit);
        if (o == null) {
            return null;
        }
        return (IMessage) o;
    }

    @Override
    public IMessage pollFromLow() {
        Object o = cacheService.lPop(LOW_QUEUE_NAME);
        if (o == null) {
            return null;
        }
        return (IMessage) o;
    }
}
