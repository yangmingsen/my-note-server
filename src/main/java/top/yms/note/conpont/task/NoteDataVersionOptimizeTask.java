package top.yms.note.conpont.task;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteContentOptimizeService;
import top.yms.note.enums.AsyncTaskEnum;

import javax.annotation.Resource;
import java.util.List;

@Component
public class NoteDataVersionOptimizeTask extends AbstractAsyncExecuteTask {

    @Resource
    private NoteContentOptimizeService noteContentOptimizeService;

    @Override
    boolean needTx() {
        return false;
    }

    @Override
    void doRun(Object data) {
        List<AsyncTask> dataList = getAllData();
        for (AsyncTask asyncTask : dataList) {
            Object taskInfo = asyncTask.getTaskInfo();
            noteContentOptimizeService.removeOneUnnecessaryVersion((Long)taskInfo);
        }
    }

    @Override
    public boolean support(AsyncTask task) {
        return task.getType() == AsyncTaskEnum.NOTE_CONTENT_VERSION_OPTIMIZE;
    }
}
