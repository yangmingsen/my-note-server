package top.yms.note.conpont.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.repo.ResourceReferenceCountRepository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <h2>资源引用检查</h2>
 * <p>检查t_note_file记录引用情况</p>
 */
//@Component
public class ResourceReferenceCountTask implements ScheduledExecuteTask, NoteTask{

    private static final Logger log = LoggerFactory.getLogger(ResourceReferenceCountTask.class);

    @Resource
    private ResourceReferenceCountRepository resourceReferenceCountRepository;

    @Override
    public void run() {
        log.info("==========ResourceReferenceCountTask 开始执行============");

    }

    @Override
    public void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService) {
        noteScheduledExecuteService.scheduleWithFixedDelay(this, 0, 30, TimeUnit.MINUTES);
        log.info("[ResourceReferenceCountTask]注册到ScheduledTask成功...");
    }
}
