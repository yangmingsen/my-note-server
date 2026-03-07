package top.yms.note.service.impl;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.entity.NetworkResourceInfo;
import top.yms.note.mapper.NetworkResourceInfoMapper;
import top.yms.note.service.NetworkResourceInfoService;

import javax.annotation.Resource;

@Component
public class NetworkResourceInfoServiceImpl implements NetworkResourceInfoService {

    @Resource(name = NoteConstants.noteRedisCacheServiceImpl)
    private NoteRedisCacheService cacheService;

    @Resource
    private NetworkResourceInfoMapper networkResourceInfoMapper;

    @Override
    public NetworkResourceInfo findOne(String noteFileId) {
        Object oV = cacheService.hGet(NoteCacheKey.NETWORK_RESOURCE_INFO_HASH, noteFileId);
        if (oV != null) return (NetworkResourceInfo)oV;
        NetworkResourceInfo networkResourceInfo = networkResourceInfoMapper.selectByPrimaryKey(noteFileId);
        cacheService.hSet(NoteCacheKey.NETWORK_RESOURCE_INFO_HASH, noteFileId, networkResourceInfo);
        return networkResourceInfo;
    }
}
