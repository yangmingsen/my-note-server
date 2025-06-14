package top.yms.note.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.task.NoteExecuteService;
import top.yms.note.conpont.task.NoteTask;
import top.yms.note.entity.NoteFile;
import top.yms.note.mapper.NoteFileMapper;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ResourceSync {

    private final static Logger log = LoggerFactory.getLogger(ResourceSync.class);

    @Resource
    private NoteFileMapper noteFileMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private NoteExecuteService noteExecuteService;

    private AtomicInteger curIdx;


    public void sync() {

        List<NoteFile> noteFiles = noteFileMapper.findAll();
        int totalSize = noteFiles.size();
        if (totalSize == 0) {
            return;
        }
        curIdx = new AtomicInteger(0);
        //execute task
        noteExecuteService.execute(new SyncMonitor(totalSize, curIdx));
        //foreach data
        for (NoteFile noteFile : noteFiles) {
            String fileId = noteFile.getFileId();
            fileStoreService.loadFile(fileId);
            curIdx.getAndIncrement();
        }
    }


    private static class SyncMonitor implements NoteTask {

        private final int totalSize;

        private final AtomicInteger curIdx;

        public SyncMonitor(int totalSize, AtomicInteger curIdx) {
            this.totalSize = totalSize;
            this.curIdx = curIdx;
        }

        @Override
        public void run() {
            log.info("=============开始同步资源=================");
            log.info("当前同步大小为：{}", totalSize);
            while (true) {
                int idx = curIdx.get();
                double progress = ((idx*1.0) / (totalSize*1.0))*100.0d;
                String  progressStr = String.format("%.2f",progress);
                log.info("progress = {}", progressStr);
                if (idx >= totalSize) {
                    break;
                }
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                }
            }
            log.info("=============结束同步资源=================");
        }
    }

}
