package top.yms.note.conpont.queue.channel;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.queue.IMessage;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


public class RedisQueueChannel implements QueueChannel{

    private final static String HIGH_QUEUE_NAME = NoteCacheKey.QUEUE_HIGH_NAME_LIST;

    private final static String MEDIUM_QUEUE_NAME = NoteCacheKey.QUEUE_MEDIUM_NAME_LIST;

    private final static String LOW_QUEUE_NAME = NoteCacheKey.QUEUE_LOW_NAME_LIST;

    @Resource
    private NoteRedisCacheService cacheService;

    @Override
    public boolean offer(IMessage iMessage) {
        if (iMessage.getPriority() == MessagePriority.HIGH) {
            Long aLong = cacheService.rPush(HIGH_QUEUE_NAME, iMessage);
            return aLong > 0L;
        } else if (iMessage.getPriority() == MessagePriority.MEDIUM) {
            Long aLong = cacheService.rPush(MEDIUM_QUEUE_NAME, iMessage);
            return aLong > 0L;
        } else if (iMessage.getPriority() == MessagePriority.LOW) {
            Long aLong = cacheService.rPush(LOW_QUEUE_NAME, iMessage);
            return aLong > 0L;
        }
        return false;
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
