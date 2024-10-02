package top.yms.note.conpont.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yangmingsen on 2024/10/3.
 */
//@Component(Constants.noteThreadPoolExecutor)
public class NoteThreadPoolExecutor implements NoteExecuteService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }
}
