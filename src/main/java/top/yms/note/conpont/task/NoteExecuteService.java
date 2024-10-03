package top.yms.note.conpont.task;

/**
 * Created by yangmingsen on 2024/10/2.
 *
 * note任务执行服务
 */
public interface NoteExecuteService {

    void execute(Runnable command);

}
