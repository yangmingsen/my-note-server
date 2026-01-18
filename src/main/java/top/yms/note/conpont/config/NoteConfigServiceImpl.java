package top.yms.note.conpont.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.conpont.SysConfigService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.entity.SystemConfig;
import top.yms.note.mapper.SystemConfigMapper;

import javax.annotation.Resource;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component
public class NoteConfigServiceImpl implements SysConfigService {

    @Resource
    private NoteRedisCacheService cacheService;

    @Resource
    private SystemConfigMapper systemConfigMapper;

    private Object getV(String key) {
        Object obj = cacheService.hGet(NoteCacheKey.SYSCFG_KEY, key);
        if (obj != null) return obj;
        synchronized (this) {
            //reQry once
            obj = cacheService.hGet(NoteCacheKey.SYSCFG_KEY, key);
            if (obj != null) return obj;
            //get from db
            SystemConfig systemConfig = systemConfigMapper.selectByConfigKey(key);
            obj = systemConfig.getConigValue();
            cacheService.hSet(NoteCacheKey.SYSCFG_KEY, key, obj);
        }
        return obj;
    }

    @Override
    public Object getObjValue(String key) {
        return getV(key);
    }

    @Override
    public String getStringValue(String key) {
        return (String)getV(key);
    }
}
