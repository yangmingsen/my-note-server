package top.yms.note.conpont.chcek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.task.NoteExecuteService;
import top.yms.note.conpont.task.NoteTask;
import top.yms.note.entity.CheckTarget;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

//@Component
public class NoteSizeComputeTask extends AbstractCheckTargetTask implements NoteTask {

    private static final Logger log = LoggerFactory.getLogger(ResourceReferenceCountTask.class);

    @Qualifier(NoteConstants.noteThreadPoolExecutor)
    @Resource
    private NoteExecuteService noteExecuteService;

    private AtomicInteger curIdx;

    private int totalSize;

    public int getSortValue() {
        return 1;
    }

    public boolean support(String name) {
        return "note-size-compute".equals(name);
    }

    @Override
    void doCheckTask(CheckTarget checkTarget) throws Exception {

    }

    @Override
    public void run() {
        log.info("=============开始NoteSizeCompute检查=================");
        while (true) {
            int idx = curIdx.get();
            double progress = ((idx*1.0) / (totalSize*1.0))*100.0d;
            String  progressStr = String.format("%.2f",progress);
            log.info("progress = {}%", progressStr);
            if (idx >= totalSize) {
                break;
            }
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
            }
        }
        log.info("=============结束NoteSizeCompute检查=================");
    }
}
