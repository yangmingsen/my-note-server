package top.yms.note.conpont.task;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteRecentVisitService;
import top.yms.note.entity.NoteIndex;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.utils.LocalThreadUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/10/9.
 */
@Component
public class NoteRecentVisitComputeTask extends AbstractAsyncExecuteTask implements NoteRecentVisitService {

    /**
     * LruCache缓存， 缓存每个用户最近访问情况，
     */
    static class LruCache extends LinkedHashMap<Long, NoteIndex> {
        private final int capacity;

        public LruCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, NoteIndex> eldest) {
            return size() > capacity;
        }

        public void put(NoteIndex note) {
            super.put(note.getId(), note);
        }

        public List<NoteIndex> getRecentVisitList() {
            List<NoteIndex> lruList = new ArrayList<>(this.values());
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
    public List<NoteIndex> getRecentVisitList() {
        Long userId = LocalThreadUtils.getUserId();
        return getRecentVisitList(userId);
    }

    @Override
    public void remove(Long id, Long userId) {
        getLruCache(userId).remove(id);
    }

    @Override
    public List<NoteIndex> getRecentVisitList(Long userId) {
        return getLruCache(userId).getRecentVisitList();
    }

    @Override
    boolean needTx() {
        return false;
    }

    @Override
    void doRun() {
        List<AsyncTask> allData = getAllData();
        for (AsyncTask at : allData) {
            Long userId = at.getUserId();
            NoteIndex ni = (NoteIndex) at.getTaskInfo();
            getLruCache(userId).put(ni);
        }
    }

    @Override
    public boolean support(AsyncTask task) {
        return task.getType() == AsyncTaskEnum.SYNC_COMPUTE_RECENT_VISIT;
    }
}
