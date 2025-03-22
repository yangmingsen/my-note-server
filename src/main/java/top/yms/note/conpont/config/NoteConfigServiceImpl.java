package top.yms.note.conpont.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.conpont.SysConfigService;
import top.yms.note.entity.SystemConfig;
import top.yms.note.mapper.SystemConfigMapper;

import javax.annotation.Resource;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component
public class NoteConfigServiceImpl implements SysConfigService {

    @Resource
    @Qualifier(NoteConstants.weakMemoryNoteCache)
    private NoteCacheService noteCacheService;

    @Resource
    private SystemConfigMapper systemConfigMapper;

    private Object getV(String key) {
        Object obj = noteCacheService.find(key);
        if (obj != null) return obj;
        SystemConfig systemConfig = systemConfigMapper.selectByConfigKey(key);
        String v = systemConfig.getConigValue();
        noteCacheService.update(key, v);

        return v;
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
