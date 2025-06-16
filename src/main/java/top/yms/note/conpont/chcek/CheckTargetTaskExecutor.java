package top.yms.note.conpont.chcek;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.task.NoteTask;
import top.yms.note.entity.CheckTarget;
import top.yms.note.mapper.CheckTargetMapper;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class CheckTargetTaskExecutor implements NoteTask, ApplicationListener<ApplicationReadyEvent> {

    private final static Logger log = LoggerFactory.getLogger(CheckTargetTaskExecutor.class);

    @Resource
    private CheckTargetMapper checkTargetMapper;

    private final AtomicBoolean status = new AtomicBoolean(false);

    private final List<CheckTargetTask> list = new LinkedList<>();

    @Override
    public void run() {
        log.info("=========开始执行check target 任务=============");
        log.info("当前status = {}", status);
        if (!status.get()) {
            List<CheckTarget> checkTargetList = checkTargetMapper.findAll();
            for (CheckTarget checkTarget : checkTargetList) {
                log.info("开始执行CheckTask={}", JSON.toJSONString(checkTarget, JSONWriter.Feature.PrettyFormat));
                try {
                    for (CheckTargetTask checkTargetTask : list) {
                        if (checkTargetTask.support(checkTarget.getName())) {
                            checkTargetTask.excTask(checkTarget);
                        }
                    }
                } catch (Exception e) {
                    log.error("执行异常", e);
                }
            }
        }
        status.set(true);
        log.info("=========结束执行check target 任务=============");
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Collection<CheckTargetTask> values = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                context, CheckTargetTask.class, true, false).values();
        list.addAll(values);
        Collections.sort(list);
        log.info("获取到CheckTargets：{}", list);
    }
}
