package top.yms.note.conpont.task;

public interface ScheduledExecuteTask extends NoteTask {

    /**
     * 注册定时任务
     * @param noteScheduledExecuteService
     */
    void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService);
}
