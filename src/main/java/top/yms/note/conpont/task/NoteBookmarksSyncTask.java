package top.yms.note.conpont.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteFetchService;
import top.yms.note.conpont.fetch.AbstractNoteFetch;
import top.yms.note.entity.NoteBookmarks;
import top.yms.note.entity.NoteMeta;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.exception.NoteSystemException;
import top.yms.note.mapper.NoteBookmarksMapper;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.msgcd.NoteSystemErrorCode;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NoteBookmarksSyncTask  extends AbstractAsyncExecuteTask implements ScheduledExecuteTask  {

    private final static Logger log = LoggerFactory.getLogger(NoteBookmarksSyncTask.class);

    private final BlockingQueue<AsyncTask> taskQueue = new ArrayBlockingQueue<>(200);

    private final Object obj = new Object();

    private volatile ThreadPoolExecutor threadPoolExecutor = null;

    private final AtomicInteger taskCnt = new AtomicInteger(0);

    @Resource
    private NoteFetchService noteFetchService;

    @Resource
    private NoteBookmarksMapper noteBookmarksMapper;

    @Resource
    private NoteMetaMapper noteMetaMapper;

    private ThreadPoolCheckTask threadPoolCheckTask = new ThreadPoolCheckTask();

    public int getSortValue() {
        return 2;
    }

    public void addTask(AsyncTask task) {
        try {
            taskQueue.put(task);
            saveTask(task);
            log.debug("addTask queue size={}", taskQueue.size());
        } catch (Exception e) {
            log.error("addTask error", e);
            throw new NoteSystemException(NoteSystemErrorCode.E_400008);
        }
        //开始执行
        if (threadPoolExecutor == null) {
            initThreadPool();
        }
        log.debug("submit task {}", task);
        threadPoolExecutor.execute(this);
    }


    @Override
    boolean needTx() {
        return true;
    }

    private void initThreadPool() {
        if (threadPoolExecutor == null) {
            synchronized (obj) {
                if (threadPoolExecutor != null) {
                    return ;
                }
                RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
                BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(200);
                threadPoolExecutor =
                        new ThreadPoolExecutor(30, 50, 60L, TimeUnit.SECONDS, queue, rejectedExecutionHandler);
            }
        }
    }

    public Object getCurrentNeedHandleData() throws Exception{
        return taskQueue.poll(3, TimeUnit.SECONDS);
    }

    protected  boolean hasData() {
        log.debug("current queue data size = {}", taskQueue.size());
        return !taskQueue.isEmpty();
    }

    @Override
    void doRun(Object data) {
        log.debug("start task ={}", data);
        if (data != null ) {
            AsyncTask pollTask = (AsyncTask)data;
            NoteBookmarks noteBookmarks = (NoteBookmarks) pollTask.getTaskInfo();
            Long id = noteBookmarks.getId();
            //add NoteMeta
            NoteMeta newNoteMeta = new NoteMeta();
            newNoteMeta.setId(noteBookmarks.getId());
            newNoteMeta.setParentId(noteBookmarks.getParentId());
            newNoteMeta.setUserId(noteBookmarks.getUserId());
            newNoteMeta.setName(noteBookmarks.getName());
            String isFile = NoteConstants.BOOKMARKS_FOLDER.equals(noteBookmarks.getType()) ? NoteConstants.DIR_FLAG : NoteConstants.FILE_FLAG;
            newNoteMeta.setIsFile(isFile);
            String noteType = NoteConstants.DIR_FLAG.equals(isFile) ? null : NoteConstants.MARKDOWN;
            newNoteMeta.setType(noteType);
            newNoteMeta.setCreateTime(new Date());
            newNoteMeta.setUpdateTime(new Date());
            newNoteMeta.setStoreSite(NoteConstants.MYSQL);
            NoteMeta noteMeta = noteMetaMapper.selectByPrimaryKey(id);
            if (noteMeta == null) {
                noteMetaMapper.insertSelective(newNoteMeta);
            } else {
                noteMetaMapper.updateByPrimaryKeySelective(newNoteMeta);
            }
            //只对url进行同步
            if (NoteConstants.BOOKMARKS_URL.equals(noteBookmarks.getType()) &&
                    NoteConstants.BOOKMARKS_SYNC_FLAG_UN.equals(noteBookmarks.getSyncFlag())) {
                // fetch markdown and save data
                AbstractNoteFetch.FetchMeta fetchMeta = new AbstractNoteFetch.FetchMeta();
                fetchMeta.setNoteIndex(newNoteMeta);
                fetchMeta.setUrl(noteBookmarks.getUrl());
                log.debug("start fetch url={}", fetchMeta);
                noteFetchService.fetch(fetchMeta, NoteConstants.MARKDOWN);
            }
            //同步完成，更新bookmarks状态
            NoteBookmarks bookmarksUpdate = new NoteBookmarks();
            bookmarksUpdate.setId(noteBookmarks.getId());
            bookmarksUpdate.setSyncFlag(NoteConstants.BOOKMARKS_SYNC_FLAG);
            bookmarksUpdate.setSyncLastTime(new Date());
            log.debug("更新bookMarks={}", bookmarksUpdate);
            noteBookmarksMapper.updateByPrimaryKeySelective(bookmarksUpdate);
        }
    }

    public void throwException(Object obj) {
        AsyncTask pollTask = (AsyncTask)obj;
        NoteBookmarks noteBookmarks = (NoteBookmarks) pollTask.getTaskInfo();
        //同步异常，更新bookmarks状态
        NoteBookmarks bookmarksUpdate = new NoteBookmarks();
        bookmarksUpdate.setId(noteBookmarks.getId());
        bookmarksUpdate.setSyncFlag(NoteConstants.BOOKMARKS_SYNC_FLAG);
        noteBookmarksMapper.updateByPrimaryKeySelective(bookmarksUpdate);
    }


    private boolean needShutdown() {
        int x = 5; //x 个 tasker 认为没有任务了
        int activeCount = threadPoolExecutor.getActiveCount();
        if (taskCnt.get() > x) {
            return true;
        }
        //如果活跃线程本来就小于 x 个，那么只要这x个都认为结束了，便可结束
        if (activeCount <= x ) {
            if (activeCount == taskCnt.get()) return true;
        }
        return false;
    }

    @Override
    public boolean support(AsyncTask task) {
        return AsyncTaskEnum.apply(task.getType().getValue()) == AsyncTaskEnum.BOOKMARKS_SYNC_TASK;
    }

    public  class ThreadPoolCheckTask implements NoteTask{

        @Override
        public void run() {
            log.debug("ThreadPoolCheckTask start at: {}", LocalDateTime.now());
            if (threadPoolExecutor != null) {
                log.debug("ThreadPoolCheckTask start at: {}, current taskQueueSize={}", LocalDateTime.now(), taskQueue.size());
                if (taskQueue.isEmpty()) {
                    taskCnt.getAndIncrement();
                } else {
                    taskCnt.set(0);
                }
                if (taskCnt.get() > 6) {
                    log.debug("---------------------shutdown threadPool----------------------");
                    threadPoolExecutor.shutdown();
                    threadPoolExecutor = null;
                    System.gc();
                }
            }
        }
    }

    @Override
    public void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService) {
        noteScheduledExecuteService.scheduleWithFixedDelay(threadPoolCheckTask, 5, 10, TimeUnit.SECONDS);
        log.info("threadPoolCheckTask注册到ScheduledTask成功...");
    }
}
