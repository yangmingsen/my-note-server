package top.yms.note.conpont.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteExpireCacheService;
import top.yms.note.conpont.task.NoteScheduledExecutorService;
import top.yms.note.conpont.task.NoteTask;
import top.yms.note.conpont.task.ScheduledExecuteTask;
import top.yms.note.exception.CommonException;
import top.yms.note.msgcd.CommonErrorCode;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component(NoteConstants.noteExpireTimeCache)
public class ExpireTimeCacheService implements NoteExpireCacheService, ScheduledExecuteTask, NoteTask {

    private static  final Logger log = LoggerFactory.getLogger(ExpireTimeCacheService.class);

    @Value("${cache.default-expire.time:120}")
    private long defaultExpireTime;

    private static class ExpireObjectEntity {
        private String key;
        private Object obj;
        private LocalDateTime expireTime;
        public ExpireObjectEntity() {        }

        public ExpireObjectEntity(String key, Object obj, LocalDateTime expireTime) {
            this.key = key;
            this.obj = obj;
            this.expireTime = expireTime;
        }

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        /**
         * 过期 true, 未过期 false
         * @return
         */
        public boolean isExpire() {
            LocalDateTime now = LocalDateTime.now();
            if (!expireTime.isAfter(now)) {
                //过期
                return true;
            }
            return false;
        }
    }

    private final Map<String, ExpireObjectEntity> expireTimeMap = new ConcurrentHashMap<>();

    @Override
    public Object find(String id) {
        ExpireObjectEntity eoe = expireTimeMap.get(id);
        if (eoe == null) {
            return null;
        }
        if (eoe.isExpire()) {
            delete(id);
            return null;
        }
        return eoe.getObj();
    }

    @Override
    public Object add(String id, Object data) {
        return add(id, data, defaultExpireTime);
    }

    @Override
    public Object delete(String id) {
        return expireTimeMap.remove(id);
    }

    @Override
    public Object update(String id, Object data) {
        return update(id, data, defaultExpireTime);
    }

    @Override
    public Object add(String id, Object data, long second) {
        Object eoe = find(id);
        if (eoe != null) {
            throw new CommonException(CommonErrorCode.E_300005);
        }
        return update(id, data, second);
    }

    @Override
    public Object update(String id, Object data, long second) {
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(second);
        ExpireObjectEntity eoe = new ExpireObjectEntity(id, data, futureTime);
        return expireTimeMap.put(id, eoe);
    }

    private int getCacheSize() {
        return expireTimeMap.size();
    }

    @Override
    public void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService) {
        noteScheduledExecuteService.scheduleWithFixedDelay(this, 5, 25, TimeUnit.SECONDS);
        log.info("{} 注册到ScheduledTask成功...", this);
    }

    @Override
    public void run() {
        if (getCacheSize() == 0) return;
        log.debug("=========开始执行key clear==========");
        log.debug("当前key size={}", getCacheSize());
        for (String key : expireTimeMap.keySet()) {
            ExpireObjectEntity eoe = expireTimeMap.get(key);
            if (eoe.isExpire()) {
                log.debug("已清理过期 cache={}", eoe);
                delete(eoe.getKey());
            }
        }
        log.debug("=========开始执行 key clear 结束==========");
    }
}
