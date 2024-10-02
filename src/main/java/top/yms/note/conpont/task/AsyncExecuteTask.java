package top.yms.note.conpont.task;

public interface AsyncExecuteTask extends Runnable{
    void addTask(AsyncTask task);
    boolean support(AsyncTask task);
}
