package top.yms.note.conpont.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;

@Component(NoteConstants.accessDelayExpireTimeCacheService)
public class AccessDelayExpireTimeCacheService extends ExpireTimeCacheService {

    @Value("${cache.access-delay-expire.time:600}")
    private long accessDelayExpireTime;

    public long getExpireTime() {
        return accessDelayExpireTime;
    }


    public Object find(String id) {
        Object o = super.find(id);
        if (o != null) {
            //执行延时
            super.update(id, o);
        }
        return o;
    }

}
