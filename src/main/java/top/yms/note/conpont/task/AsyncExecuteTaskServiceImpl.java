package top.yms.note.conpont.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteAsyncExecuteTaskService;
import top.yms.note.conpont.NoteQueue;
import top.yms.note.enums.AsyncExcuteTypeEnum;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/10/2.
 */
@Component
public class AsyncExecuteTaskServiceImpl implements NoteAsyncExecuteTaskService, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(AsyncExecuteTaskServiceImpl.class);

    private final List<AsyncExecuteTask> asyncExecuteTaskList = new LinkedList<>();


    @Qualifier(Constants.noteScheduledThreadPoolExecutor)
    @Autowired
    private NoteExecuteService noteExecuteService;


    @Override
    public void addTask(AsyncTask task) {
        for (AsyncExecuteTask executeTask : asyncExecuteTaskList) {
            if (executeTask.support(task)) {
                executeTask.addTask(task);
                //开始执行
                executeTask(task, executeTask);
                break;
            }
        }
    }

    /**
     * 执行同步任务
     * @param task
     * @param asyncExecuteTask
     */
    private void executeTask(AsyncTask task, AsyncExecuteTask asyncExecuteTask) {
        if (AsyncExcuteTypeEnum.apply(task.getExecuteType().getValue()) == AsyncExcuteTypeEnum.SYNC_TASK) {
            noteExecuteService.execute(asyncExecuteTask);
        } else if (AsyncExcuteTypeEnum.apply(task.getExecuteType().getValue()) == AsyncExcuteTypeEnum.CALLER_TASK) {
            AbstractAsyncExecuteTask abTask = (AbstractAsyncExecuteTask)asyncExecuteTask;
            abTask.run();
        }
    }


    /**
     * 初始化
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        asyncExecuteTaskList.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        context, AsyncExecuteTask.class, true, false).values());
        log.info("获取到 AsyncExecuteTask: {}", asyncExecuteTaskList);

        //注册定时任务
        regScheduledTask();
    }


    /**
     * 注册定时执行任务
     */
    private void regScheduledTask() {
        for (AsyncExecuteTask executeTask : this.asyncExecuteTaskList) {
            if (executeTask instanceof ScheduledExecuteTask) {
                ((ScheduledExecuteTask) executeTask).regScheduledTask((NoteScheduledExecutorService)this.noteExecuteService);
            }
        }
    }
}
