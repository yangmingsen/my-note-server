package top.yms.note.conpont.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangmingsen on 2024/10/2.
 */
@Component(NoteConstants.noteScheduledThreadPoolExecutor)
public class NoteScheduledThreadPoolExecutor implements NoteScheduledExecutorService {

    @Value("${system.task.exc_work_num}")
    private int coreCpuNum;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(coreCpuNum);


    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledExecutorService.schedule(command, delay, unit);
    }

    @Override
    public void execute(Runnable command) {
        scheduledExecutorService.execute(command);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
