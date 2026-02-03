package top.yms.note.conpont.crawler.impl;

import org.springframework.beans.BeanUtils;
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
            //#这里改出了个大bug,这里直接给networkNote的content给null，导致后面mysql数据同步时拿不到content。 额.....
            //解决方案是：使用新bean来
            NetworkNote networkNote1 = new NetworkNote();
            BeanUtils.copyProperties(networkNote, networkNote1);
            networkNote1.setContent(null);
            //不需要再存储内容了，因为mysql已经存储了
            networkNoteRepository.save(networkNote1);
            //add ok to cache
            String url = networkNote.getUrl();
            cacheService.sAdd(NoteCacheKey.CRAWLER_SUCCESS_SET, url);
            //从待爬取set中删除
            cacheService.sRem(NoteCacheKey.CRAWLER_DUP_SET, url);
        }
    }
}
