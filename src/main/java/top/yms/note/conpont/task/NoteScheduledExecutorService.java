package top.yms.note.conpont.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface NoteScheduledExecutorService extends NoteExecuteService {
     ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);


    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit);
}
