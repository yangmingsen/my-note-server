package top.yms.note.conpont.crawler.scheduler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.crawler.worker.CrawlWorkerQueue;
import top.yms.note.conpont.task.NoteTask;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * URL 调度器（去重 + 队列）
 */
@Component
public class DefaultUrlScheduler implements  UrlScheduler, NoteTask {

    private final static Logger log = LoggerFactory.getLogger(DefaultUrlScheduler.class);

    /**
     * 存放待处理列表
     */
    private  BlockingQueue<String> queue ;

    @Value("${crawler.status}")
    private boolean crawlerStatus;

    @Resource
    private NoteRedisCacheService cacheService;

    /**
     * 分发线程
     */
    private Thread deliveryTask = null;

    /**
     * 保存待分发队列
     */
    private final List<CrawlWorkerQueue> crawlWorkerQueueList = new LinkedList<>();

    /**
     * 保存是否初始化标记
     */
    private volatile boolean initFlg = false;

    private boolean getInitFlg() {
        return initFlg;
    }

    /**
     * 初始化
     */
    private void doInit() {
        if (!getInitFlg()) {
            synchronized (this) {
                doTaskPrepare();
                initFlg = true;
            }
        }
    }

    @Override
    public void regCrawlWorkerQueue(CrawlWorkerQueue crawlWorkerQueue) {
        crawlWorkerQueueList.add(crawlWorkerQueue);
    }

    @Override
    public void clear() {
        //只有初始化过，才能执行clear
        if (getInitFlg()) {
            crawlWorkerQueueList.clear();
            queue.clear();
            //handle cache
            Set<Object> successSet = cacheService.sMembers(NoteCacheKey.CRAWLER_SUCCESS_SET);
            for (Object url : successSet) {
                cacheService.sAdd(NoteCacheKey.CRAWLER_DUP_SET, url);
            }
        }
    }

    @Override
    public boolean support(String url) {
        return false;
    }

    public void add(String url) {
        Boolean isExist = cacheService.sIsMember(NoteCacheKey.CRAWLER_DUP_SET, url);
        if (!isExist) {
            queue.offer(url);
            cacheService.sAdd(NoteCacheKey.CRAWLER_DUP_SET, url);
        }
    }

    /**
     * 一般首次，使用该方法，可以作为init的入口点
     * @param url
     */
    public void addForRoot(String url) {
        //for init
        doInit();
        //add url to queue
        queue.offer(url);
    }

    public String take()  {
        try {
            return queue.take();
        } catch (Exception e) {
            log.error("take url error: {}", e.getMessage());
            return null;
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    private void doTaskPrepare() {
        if (crawlerStatus) {
            Set<Object> successSet = cacheService.sMembers(NoteCacheKey.CRAWLER_SUCCESS_SET);
            for (Object url : successSet) {
                cacheService.sAdd(NoteCacheKey.CRAWLER_DUP_SET, url);
            }
            //new queue
            queue = new ArrayBlockingQueue<>(9000);
            //开启一个分发线程
            log.info("==========启动deliveryTask===========");
            deliveryTask = new Thread(this);
            deliveryTask.start();
            log.info("==========启动deliveryTask Ok========");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String url = take();
                if (StringUtils.isBlank(url)) {
                    continue;
                }
                boolean isSupport = false; //检查是否有支持该数据的队列，若无则继续放入队列中
                for (CrawlWorkerQueue crawlWorkerQueue : crawlWorkerQueueList) {
                    if (crawlWorkerQueue.support(url)) {
                        isSupport = true;
                        boolean ok = crawlWorkerQueue.offer(url);
                        if (!ok) {
                            //如果不成功，则再入队列，等待下次分发
                            queue.offer(url);
                        }
                    }
                }
                if (!isSupport) {
                    queue.offer(url);
                }
            } catch (Throwable th) {
                log.error("分发任务异常： {}", th.getMessage());
            }
        }
    }
}

