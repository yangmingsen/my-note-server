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
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteAsyncExecuteTaskService;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.exception.BusinessException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/10/2.
 */
@Component
public class AsyncExecuteTaskServiceImpl implements NoteAsyncExecuteTaskService, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(AsyncExecuteTaskServiceImpl.class);

    private final List<AsyncExecuteTask> asyncExecuteTaskList = new LinkedList<>();


    /**
     * 执行器
     */
    @Qualifier(NoteConstants.noteScheduledThreadPoolExecutor)
    @Autowired
    private NoteScheduledExecutorService noteScheduledExecutorService;


    @Override
    public void addTask(AsyncTask task) {
        if (task.getType() == null) {
            log.error("addTask# 异步任务类型为空");
            throw new BusinessException(CommonErrorCode.E_300001);
        }
        if (task.getExecuteType() == null) {
            log.error("addTask# 执行#异步任务类型为空");
            throw new BusinessException(CommonErrorCode.E_300002);
        }
        for (AsyncExecuteTask executeTask : asyncExecuteTaskList) {
            if (executeTask.support(task)) {
                executeTask.addTask(task);
                //开始执行
                executeTask(task, executeTask);
                return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200216);
    }

    /**
     * 执行同步任务
     * @param task
     * @param asyncExecuteTask
     */
    private void executeTask(AsyncTask task, AsyncExecuteTask asyncExecuteTask) {
        //如果当前正在运行任务, 离开
        if (((AbstractAsyncExecuteTask)asyncExecuteTask).isRun()) {
            log.info("{}#当前任务正在执行...", asyncExecuteTask);
            return;
        }

        //没有运行的话，执行任务
        if (task.getExecuteType() == AsyncExcuteTypeEnum.SYNC_TASK) {
            noteScheduledExecutorService.execute(asyncExecuteTask);
        } else if (task.getExecuteType() == AsyncExcuteTypeEnum.CALLER_TASK) {
            AbstractAsyncExecuteTask abTask = (AbstractAsyncExecuteTask)asyncExecuteTask;
            abTask.run();
        } else if (task.getExecuteType() == AsyncExcuteTypeEnum.DELAY_EXC_TASK) {
            //我希望交给具体执行类，由它决定延时到什么时候执行
            if (asyncExecuteTask instanceof DelayExecuteTask) {
                if (task instanceof DelayExecuteAsyncTask) {
                    ((DelayExecuteTask)asyncExecuteTask).delayExecute(this.noteScheduledExecutorService, (DelayExecuteAsyncTask) task);
                } else {
                    log.error("当前AsyncTask非DelayExecuteAsyncTask");
                    throw new BusinessException(CommonErrorCode.E_300004);
                }
            } else {
                log.error("当前执行任务类非DelayExecuteTask");
                throw new BusinessException(CommonErrorCode.E_300003);
            }
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
        Collections.sort(asyncExecuteTaskList);
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
                ((ScheduledExecuteTask) executeTask).regScheduledTask(this.noteScheduledExecutorService);
            }
        }
    }
}
