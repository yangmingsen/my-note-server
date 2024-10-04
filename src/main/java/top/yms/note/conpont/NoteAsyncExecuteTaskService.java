package top.yms.note.conpont;

import top.yms.note.conpont.task.AsyncTask;

/**
 * <h3>异步任务执行服务</h3>
 * <p>任务执行方式：1.使用线程池立即执行.  3.使用调用者线程执行</p>
 * <p></p>
 */
public interface NoteAsyncExecuteTaskService {

    /**
     * 使用方应该直接调用该方法添加任务
     * @param task
     */
    void addTask(AsyncTask task);

}
