package top.yms.note.conpont.task;

public interface DelayExecuteTask {

    void delayExecute(NoteScheduledExecutorService noteScheduledExecutorService, DelayExecuteAsyncTask delayExecuteAsyncTask);
}
