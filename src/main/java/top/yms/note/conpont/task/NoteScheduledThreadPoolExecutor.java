package top.yms.note.conpont.task;

import org.springframework.stereotype.Component;
import top.yms.note.comm.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangmingsen on 2024/10/2.
 */
@Component(Constants.noteScheduledThreadPoolExecutor)
public class NoteScheduledThreadPoolExecutor implements NoteScheduledExecutorService {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

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
