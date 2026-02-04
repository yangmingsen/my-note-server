package top.yms.note.conpont.sync.network;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteMetaExample;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CnBlogsNetworkNoteSyncServiceImpl extends AbstractNetworkNoteSyncService implements MessageListener {

    private static final String secondLevelName = "博客园（CnBlogs)";

    private static final String secondLevelName2 = "博客园（CnBlogs)2";

    private Long rootLevelId;

    private Long secondLevelId;

    private Long secondLevelId2;

    @Resource
    private NoteRedisCacheService cacheService;

    private int autoInCream = 0;

    private int hashSplice = 500;


    @Override
    Long getSecondLevelId(Long parentId) {
        if (rootLevelId == null) {
            rootLevelId = parentId;
        }
        if (secondLevelId != null) {
            return secondLevelId;
        }
        secondLevelId = findOrCreate(secondLevelName, parentId);
        return secondLevelId;
    }

    @Override
    Long getThirdLevelId(Long parentId, String param) {
        String thirdLevelName = extractLevelDirectory(param);
        //查询是否存在
        NoteMetaExample example = new NoteMetaExample();
        NoteMetaExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(thirdLevelName);
        criteria.andParentIdEqualTo(parentId);
        criteria.andDelEqualTo(NoteConstants.UN_DELETE_FLAG);
        List<NoteMeta> noteMetas = noteMetaMapper.selectByExample(example);
        boolean isExist = true;
        if (noteMetas.isEmpty()) {
            isExist = false;
        }
        Long thirdLevelId = null;
        if (isExist) {
            thirdLevelId= noteMetas.get(0).getId();
        } else {
            //不存在的话，使用新的hash算法计算位置
            if (secondLevelId2 == null) {
                secondLevelId2 = findOrCreate(secondLevelName2, rootLevelId);
            }
            Object oV = cacheService.hGet(NoteCacheKey.SYNC_NETNOTE_CNBLOGS_LEVEL_NAME_HASH, thirdLevelName);
            String hashLevelName = null;
            if (oV == null) {
                int hashId = autoInCream%hashSplice;
                autoInCream++;
                hashLevelName = String.format("%03d", hashId);
                cacheService.hSet(NoteCacheKey.SYNC_NETNOTE_CNBLOGS_LEVEL_NAME_HASH, thirdLevelName, hashLevelName);
            } else {
                hashLevelName = (String) oV;
            }
            Long hashLevelId = findOrCreate(hashLevelName, secondLevelId2);
            //找到真正存储位置
            thirdLevelId = findOrCreate(thirdLevelName, hashLevelId);
        }
        return thirdLevelId;
    }

    @Override
    public boolean support(IMessage message) {
        if (message instanceof NetworkNoteMessage) {
            NetworkNote networkNote = (NetworkNote)message.getBody();
            String url = networkNote.getUrl();
            if (url.contains("www.cnblogs.com")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMessage(IMessage message) {
        NetworkNote networkNote = (NetworkNote)message.getBody();
        doDataSync(networkNote);
    }
}
