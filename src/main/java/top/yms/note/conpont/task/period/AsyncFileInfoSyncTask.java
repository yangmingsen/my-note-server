package top.yms.note.conpont.task.period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.MessageListener;
import top.yms.note.conpont.queue.imsg.AsyncFileInfoMessage;
import top.yms.note.conpont.store.AsyncFileSaveDto;
import top.yms.note.conpont.task.NoteScheduledExecutorService;
import top.yms.note.conpont.task.ScheduledExecuteTask;
import top.yms.note.entity.AsyncFileSaveInfo;
import top.yms.note.entity.NetworkResourceInfo;
import top.yms.note.mapper.NetworkResourceInfoMapper;
import top.yms.note.repo.AsyncFileSaveInfoRepository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class AsyncFileInfoSyncTask implements ScheduledExecuteTask , MessageListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncFileInfoSyncTask.class);

    @Resource(name = NoteConstants.noteRedisCacheServiceImpl)
    private NoteRedisCacheService cacheService;

    @Resource
    private AsyncFileSaveInfoRepository asyncFileSaveInfoRepository;

    @Resource
    private NetworkResourceInfoMapper networkResourceInfoMapper;

    private int maxTime = 3; //最大重试次数


    @Override
    public void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService) {
        noteScheduledExecuteService.scheduleWithFixedDelay(this, 1, 60, TimeUnit.MINUTES);
    }

    private void saveToDb(AsyncFileSaveDto fileSaveDto) {
        NetworkResourceInfo networkResourceInfo = new NetworkResourceInfo();
        String noteFileId = fileSaveDto.getNoteFileId();
        networkResourceInfo.setNoteFileId(noteFileId);
        networkResourceInfo.setUrl(fileSaveDto.getFetchUrl());
        networkResourceInfo.setSuffix(fileSaveDto.getSuffix());
        networkResourceInfo.setName(fileSaveDto.getTmpFileName());
        networkResourceInfo.setCreateTime(new Date());
        //check
        NetworkResourceInfo oldNRI = networkResourceInfoMapper.selectByPrimaryKey(noteFileId);
        if (oldNRI == null) {
            networkResourceInfoMapper.insertSelective(networkResourceInfo);
        } else {
            networkResourceInfoMapper.updateByPrimaryKeySelective(networkResourceInfo);
        }
    }

    @Override
    public void run() {
        /* 不再使用
        int breakCond = 0;
        while (true) {
            try {
                Object oV = cacheService.blPop(NoteCacheKey.ASYNC_UPLOAD_FILE_LIST, 15, TimeUnit.SECONDS);
                if (oV == null) {
                    breakCond++;
                    if (breakCond >= this.maxTime) {
                        log.info("超过最大重试次数{}, 退出", maxTime);
                        break;
                    }
                    continue;
                }
                AsyncFileSaveDto fileSaveDto = (AsyncFileSaveDto) oV;
                saveToDb(fileSaveDto);
            } catch (Exception e) {
                log.error("AsyncFileInfoSyncTask error", e);
            }
        }*/
    }

    @Override
    public boolean support(IMessage message) {
        return message instanceof AsyncFileInfoMessage;
    }

    @Override
    public void onMessage(IMessage message) {
        AsyncFileSaveDto body = (AsyncFileSaveDto)message.getBody();
        saveToDb(body);
    }
}
