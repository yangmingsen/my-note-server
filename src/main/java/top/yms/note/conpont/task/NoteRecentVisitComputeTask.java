package top.yms.note.conpont.task;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteRecentVisitService;
import top.yms.note.conpont.SensitiveService;
import top.yms.note.entity.NoteMeta;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/10/9.
 */
@Component
public class NoteRecentVisitComputeTask extends AbstractAsyncExecuteTask implements NoteRecentVisitService {

    @Resource
    private SensitiveService sensitiveService;

    /**
     * LruCache缓存， 缓存每个用户最近访问情况，
     */
    static class LruCache extends LinkedHashMap<Long, NoteMeta> {
        private final int capacity;

        public LruCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, NoteMeta> eldest) {
            return size() > capacity;
        }

        public void put(NoteMeta note) {
            super.put(note.getId(), note);
        }

        public List<NoteMeta> getRecentVisitList() {
            List<NoteMeta> lruList = new ArrayList<>(this.values());
            Collections.reverse(lruList);
            return lruList;
        }
    }

    /**
     * <userId <-> LruCache
     */
    private final Map<Long, LruCache> lruCacheMap = new ConcurrentHashMap<>();


    public int getSortValue() {
        return 2;
    }


    private LruCache getLruCache(Long userId) {
        LruCache lruCache = lruCacheMap.get(userId);
        if (lruCache == null) {
            lruCache = new LruCache(20);
            lruCacheMap.put(userId, lruCache);
        }
        return lruCache;
    }

    @Override
    public List<NoteMeta> getRecentVisitList() {
        Long userId = LocalThreadUtils.getUserId();
        return getRecentVisitList(userId);
    }

    @Override
    public void remove(Long id, Long userId) {
        getLruCache(userId).remove(id);
    }

    @Override
    public List<NoteMeta> getRecentVisitList(Long userId) {
        return getLruCache(userId).getRecentVisitList();
    }

    @Override
    boolean needTx() {
        return false;
    }

    @Override
    void doRun(Object data) {
        List<AsyncTask> allData = getAllData();
        for (AsyncTask at : allData) {
            Long userId = at.getUserId();
            NoteMeta ni = (NoteMeta) at.getTaskInfo();
            if (!sensitiveService.isSensitive(ni.getId())) {
                //没有命中敏感内容
                getLruCache(userId).put(ni);
            }
        }
    }

    @Override
    public boolean support(AsyncTask task) {
        return task.getType() == AsyncTaskEnum.SYNC_COMPUTE_RECENT_VISIT;
    }
}
