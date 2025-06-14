package top.yms.note.conpont.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteAsyncExecuteTaskService;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;

import javax.annotation.Resource;
import java.util.Collection;
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
    @Resource
    private NoteScheduledExecutorService noteScheduledExecutorService;


    @Override
    public void addTask(AsyncTask task) {
        log.debug("addTask taskInfo={}", task);
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
            log.debug("{}#当前任务正在执行...", asyncExecuteTask);
            return;
        }
        //没有运行的话，执行任务
        if (task.getExecuteType() == AsyncExcuteTypeEnum.SYNC_TASK) {
            log.debug("SYNC_TASK={}", JSON.toJSONString(task, JSONWriter.Feature.PrettyFormat));
            noteScheduledExecutorService.execute(asyncExecuteTask);
        } else if (task.getExecuteType() == AsyncExcuteTypeEnum.CALLER_TASK) {
            log.debug("CALLER_TASK={}", JSON.toJSONString(task, JSONWriter.Feature.PrettyFormat));
            AbstractAsyncExecuteTask abTask = (AbstractAsyncExecuteTask)asyncExecuteTask;
            abTask.run();
        } else if (task.getExecuteType() == AsyncExcuteTypeEnum.DELAY_EXC_TASK) {
            log.debug("DELAY_EXC_TASK={}", JSON.toJSONString(task, JSONWriter.Feature.PrettyFormat));
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
        regScheduledTask(context);
    }


    /**
     * 注册定时执行任务
     */
    private void regScheduledTask(ApplicationContext context) {
        Collection<ScheduledExecuteTask> scheduledExecuteTasks = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                context, ScheduledExecuteTask.class, true, false).values();
        for (ScheduledExecuteTask executeTask : scheduledExecuteTasks) {
            executeTask.regScheduledTask(this.noteScheduledExecutorService);
        }
    }
}
