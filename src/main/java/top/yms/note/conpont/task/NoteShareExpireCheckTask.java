package top.yms.note.conpont.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteShareService;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.entity.NoteMeta;
import top.yms.note.service.NoteMetaService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class NoteShareExpireCheckTask implements ScheduledExecuteTask{

    private static  final Logger log = LoggerFactory.getLogger( NoteShareExpireCheckTask.class);

    @Resource
    private NoteShareService noteShareService;

    @Resource
    private NoteMetaService noteMetaService;

    @Override
    public void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService) {
        log.info("reg NoteShareExpireCheckTask....");
        noteScheduledExecuteService.scheduleWithFixedDelay(this, 1, 30, TimeUnit.MINUTES);
    }

    /**
     * 过期时间判断
     * @param noteMeta
     * @return
     */
    private boolean isShareExpire(NoteMeta noteMeta) {
        Date shareExpireTime = noteMeta.getShareExpireTime();
        if (shareExpireTime == null) {
            //处理旧数据，若是存在分享默认关闭
            return true;
        }
        Date curDate = new Date();
        if (curDate.compareTo(shareExpireTime) > 0) { //当前时间大于过期时间
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        log.info("start share expire check...");
        List<NoteMeta> shareList = noteMetaService.findBy(NoteIndexQuery.Builder.build().share(NoteConstants.SHARE_FLAG).get());
        log.info("shareList size={}", shareList.size());
        if (shareList.size() == 0) {
            return;
        }
        log.info("shareList ids={}", shareList.stream().map(NoteMeta::getId).collect(Collectors.toList()));
        for (NoteMeta noteMeta : shareList) {
            try {
                boolean shareExpire = isShareExpire(noteMeta);
                Long noteId = noteMeta.getId();
                log.info("NoteId {} expireTime={} checkResult={}", noteId, noteMeta.getShareExpireTime(), shareExpire);
                if (shareExpire) {
                    noteShareService.shareNoteClose(noteMeta.getId());
                }
            } catch (Exception e) {
                log.error("note share expire check error", e);
            }
        }

    }
}
