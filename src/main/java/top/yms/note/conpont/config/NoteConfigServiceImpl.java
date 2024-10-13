package top.yms.note.conpont.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCache;
import top.yms.note.conpont.SysConfigService;
import top.yms.note.entity.SystemConfig;
import top.yms.note.mapper.SystemConfigMapper;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component
public class NoteConfigServiceImpl implements SysConfigService {

    @Autowired
    @Qualifier(NoteConstants.weakMemoryNoteCache)
    private NoteCache noteCache;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    private Object getV(String key) {
        Object obj = noteCache.find(key);
        if (obj != null) return obj;
        SystemConfig systemConfig = systemConfigMapper.selectByConfigKey(key);
        String v = systemConfig.getConigValue();
        noteCache.update(key, v);

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
