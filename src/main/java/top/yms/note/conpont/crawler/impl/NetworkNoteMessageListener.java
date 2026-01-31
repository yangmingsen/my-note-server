package top.yms.note.conpont.crawler.impl;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.entity.NetworkNote;
import top.yms.note.repo.NetworkNoteRepository;

import javax.annotation.Resource;

@Component
public class NetworkNoteMessageListener implements MessageListener {

    @Resource
    private NetworkNoteRepository networkNoteRepository;

    @Resource
    private NoteRedisCacheService cacheService;

    @Override
    public boolean support(IMessage message) {
        return message instanceof NetworkNoteMessage;
    }

    @Override
    public void onMessage(IMessage message) {
        NetworkNote networkNote = (NetworkNote)message.getBody();
        NetworkNote oV = networkNoteRepository.findByMd5Id(networkNote.getMd5Id());
        if (oV == null) {
            networkNoteRepository.save(networkNote);
            //add ok to cache
            String url = networkNote.getUrl();
            cacheService.sAdd(NoteCacheKey.CRAWLER_SUCCESS_SET, url);
            //从待爬取set中删除
            cacheService.sRem(NoteCacheKey.CRAWLER_DUP_SET, url);
        }
    }
}
