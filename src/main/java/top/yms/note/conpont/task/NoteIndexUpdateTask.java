package top.yms.note.conpont.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.enums.AsyncTaskEnum;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component
public class NoteIndexUpdateTask extends AbstractAsyncExecuteTask implements DelayExecuteTask{

    private final static Logger log = LoggerFactory.getLogger(NoteIndexUpdateTask.class);

    @Value("${system.task.index_exc_delay_time}")
    private long excDelayTime;

    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    @Autowired
    private NoteDataIndexService noteDataIndexService;

    final Object obj = new Object();

    @Override
    boolean needTx() {
        return false;
    }

    @Override
    void doRun() {
        List<Long> ids = getAllData().stream().map(x -> (Long) x.getTaskInfo()).distinct().collect(Collectors.toList());
        log.debug("Exc: ids={}", ids);

        //这里进行索引更新时必须要加锁进行
        synchronized (obj) {
            noteDataIndexService.updateByIds(ids);
            log.debug("NoteIndexUpdateTask#更新完成....{}", ids);
        }

    }

    @Override
    public boolean support(AsyncTask task) {
        return AsyncTaskEnum.SYNC_Note_Index_UPDATE == task.getType();
    }


    @Override
    public void delayExecute(NoteScheduledExecutorService noteScheduledExecutorService, DelayExecuteAsyncTask delayExecuteAsyncTask) {
        log.info("delayExecute_excDelayTime={}_timeUnit={}", excDelayTime, timeUnit);
        noteScheduledExecutorService.schedule(this, excDelayTime, timeUnit);
    }
}
