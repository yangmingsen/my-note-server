package top.yms.note.conpont.task;


import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component(NoteConstants.noteThreadPoolExecutor)
public class DefaultNoteExecuteService implements NoteExecuteService {

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(100), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}
