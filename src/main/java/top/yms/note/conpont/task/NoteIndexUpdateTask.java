package top.yms.note.conpont.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.NoteIndexLuceneUpdateDto;
import top.yms.note.enums.AsyncTaskEnum;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component
public class NoteIndexUpdateTask extends AbstractAsyncExecuteTask implements DelayExecuteTask{

    private final static Logger log = LoggerFactory.getLogger(NoteIndexUpdateTask.class);

    @Value("${system.task.index_exc_delay_time}")
    private long excDelayTime;

    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    @Autowired
    private NoteDataIndexService noteDataIndexService;

    private final static Object obj = new Object();

    @Override
    boolean needTx() {
        return false;
    }

    @Override
    void doRun() {
        List<AsyncTask> allData = getAllData();
        //这里进行索引更新时必须要加锁进行
        synchronized (obj) {
            List<Long> contentUpdateList = new LinkedList<>();
            List<NoteLuceneIndex> noteIndexList = new LinkedList<>();
            List<Long> deleteList = new LinkedList<>();

            for (AsyncTask task : allData) {
                NoteIndexLuceneUpdateDto idxDto = (NoteIndexLuceneUpdateDto) task.getTaskInfo();
                if (idxDto.getType() == NoteIndexLuceneUpdateDto.updateNoteContent) {
                    contentUpdateList.add((Long)idxDto.getData());
                } else if (idxDto.getType() == NoteIndexLuceneUpdateDto.updateNoteIndex) {
                    noteIndexList.add((NoteLuceneIndex) idxDto.getData());
                } else if (idxDto.getType() == NoteIndexLuceneUpdateDto.deleteOne) {
                    deleteList.add((Long)idxDto.getData());
                } else if (idxDto.getType() == NoteIndexLuceneUpdateDto.deleteList) {
                    deleteList.addAll((List<Long>)idxDto.getData());
                }
            }

            if (contentUpdateList.size() > 0)
            noteDataIndexService.updateByIds(contentUpdateList);
            noteDataIndexService.update(noteIndexList);
            noteDataIndexService.delete(deleteList);
            log.debug("NoteIndexUpdateTask#更新完成....");
        }

    }


    private void handleIndexUpdate(List<AsyncTask> allData) {
        List<Long> contentUpdateList = new LinkedList<>();
        List<NoteLuceneIndex> noteIndexList = new LinkedList<>();
        List<Long> deleteList = new LinkedList<>();

        for (AsyncTask task : allData) {
            NoteIndexLuceneUpdateDto idxDto = (NoteIndexLuceneUpdateDto) task.getTaskInfo();
            if (idxDto.getType() == NoteIndexLuceneUpdateDto.updateNoteContent) {
                contentUpdateList.add((Long)idxDto.getData());
            } else if (idxDto.getType() == NoteIndexLuceneUpdateDto.updateNoteIndex) {
                noteIndexList.add((NoteLuceneIndex) idxDto.getData());
            } else if (idxDto.getType() == NoteIndexLuceneUpdateDto.deleteOne) {
                deleteList.add((Long)idxDto.getData());
            } else if (idxDto.getType() == NoteIndexLuceneUpdateDto.deleteList) {
                deleteList.addAll((List<Long>)idxDto.getData());
            }
        }

        noteDataIndexService.updateByIds(contentUpdateList);
        noteDataIndexService.update(noteIndexList);
        noteDataIndexService.delete(deleteList);
    }


    @Override
    public boolean support(AsyncTask task) {
        return AsyncTaskEnum.SYNC_Note_Index_UPDATE == task.getType();
    }


    @Override
    public void delayExecute(NoteScheduledExecutorService noteScheduledExecutorService, DelayExecuteAsyncTask delayExecuteAsyncTask) {
        log.debug("delayExecute_excDelayTime={}_timeUnit={}", excDelayTime, timeUnit);
        noteScheduledExecutorService.schedule(this, excDelayTime, timeUnit);
    }
}
