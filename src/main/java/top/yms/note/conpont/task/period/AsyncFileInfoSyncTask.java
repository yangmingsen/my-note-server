package top.yms.note.conpont.task.period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.store.AsyncFileSaveDto;
import top.yms.note.conpont.task.NoteScheduledExecutorService;
import top.yms.note.conpont.task.ScheduledExecuteTask;
import top.yms.note.entity.AsyncFileSaveInfo;
import top.yms.note.repo.AsyncFileSaveInfoRepository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class AsyncFileInfoSyncTask implements ScheduledExecuteTask {

    private static final Logger log = LoggerFactory.getLogger(AsyncFileInfoSyncTask.class);

    @Resource
    private NoteRedisCacheService cacheService;

    @Resource
    private AsyncFileSaveInfoRepository asyncFileSaveInfoRepository;

    private int maxTime = 3; //最大重试次数


    @Override
    public void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService) {
        noteScheduledExecuteService.scheduleWithFixedDelay(this, 1, 60, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        int breakCond = 0;
        while (true) {
            try {
                Object oV = cacheService.blPop(NoteCacheKey.ASYNC_UPLOAD_FILE_LIST, 15, TimeUnit.SECONDS);
                //再看看 之前失败的场景
                if (oV == null) {
                    oV = cacheService.blPop(NoteCacheKey.ASYNC_UPLOAD_FILE_FAIL_DEAD_LIST, 15, TimeUnit.SECONDS);
                }
                if (oV == null) {
                    breakCond++;
                    if (breakCond >= this.maxTime) {
                        log.info("超过最大重试次数{}, 退出", maxTime);
                        break;
                    }
                    continue;
                }
                AsyncFileSaveDto fileSaveDto = (AsyncFileSaveDto) oV;
                AsyncFileSaveInfo asyncFileSaveInfo = new AsyncFileSaveInfo();
                BeanUtils.copyProperties(fileSaveDto, asyncFileSaveInfo);
                asyncFileSaveInfoRepository.save(asyncFileSaveInfo);
            } catch (Exception e) {
                log.error("AsyncFileInfoSyncTask error", e);
            }
        }
    }
}
