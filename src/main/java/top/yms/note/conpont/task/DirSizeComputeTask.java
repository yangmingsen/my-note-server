package top.yms.note.conpont.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.entity.NoteMeta;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.service.NoteMetaService;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DirSizeComputeTask extends AbstractAsyncExecuteTask{

    private static final Logger log = LoggerFactory.getLogger(DirSizeComputeTask.class);

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Qualifier(NoteConstants.accessDelayExpireTimeCacheService)
    @Resource
    private NoteCacheService noteCacheService;

    @Resource
    private NoteMetaService noteMetaService;

    @Resource
    private NoteRedisCacheService cacheService;

    private final Object flagObj = new Object();

    public boolean needTx() {
        return true;
    }

    @Override
    void doRun(Object data) {
        List<AsyncTask> dataList = getAllData();
        for (AsyncTask asyncTask : dataList) {
            List<Long> ids = (List<Long>) asyncTask.getTaskInfo();
            log.debug("DirSizeCompute ids={}", ids);
            for (Long id : ids) {
                String cacheKey = NoteCacheKey.DIR_SIZE_COMPUTE_TASK_KEY+id;
                Object o = noteCacheService.find(cacheKey);
                if (o == null) {
                    computeSize(id);
                    noteCacheService.add(cacheKey, flagObj);
                }
            }
        }
    }

    private long computeSize(Long id) {
        List<NoteMeta> noteMetas = noteMetaService.selectByParentId(id);
        long size = 0;
        for (NoteMeta noteMeta : noteMetas) {
            if (NoteConstants.DIR_FLAG.equals(noteMeta.getIsFile())) {
                size += computeSize(noteMeta.getId());
            } else {
                size += noteMeta.getSize();
            }
        }
        //更新当前目录大小
        NoteMeta tmpNoteMeata = new NoteMeta();
        tmpNoteMeata.setId(id);
        tmpNoteMeata.setSize(size);
        noteMetaMapper.updateByPrimaryKeySelective(tmpNoteMeata);
        //update cache
        cacheService.hDel(NoteCacheKey.NOTE_META_LIST_KEY, id.toString());
        log.debug("update dir id={} size={}", id, size);
        //ret Size
        return size;
    }

    @Override
    public boolean support(AsyncTask task) {
        return task.getType() == AsyncTaskEnum.NOTE_DIR_SIZE_COMPUTE_TASK;
    }
}
