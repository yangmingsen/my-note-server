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
    private  ArrayBlockingQueue<String> queue ;

    /**
     * 当前申请的内容队列大小
     */
    private  int maxQueueSize = 12000;

    /**
     * 可入队因子
     */
    private int factor = 95;

    @Value("${crawler.status}")
    private boolean crawlerStatus;

    @Resource
    private NoteRedisCacheService cacheService;

    /**
     * 待入队队列是否含有数据（true-有)
     */
    private boolean waitEnQueueFlg = false;

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
            if (queue != null) queue.clear();
            //handle cache
            reDoLast();
        }
    }

    @Override
    public int queueSize() {
        return queue.size();
    }

    @Override
    public boolean canEnqueue() {
        int qs = queueSize();
        int percent = (qs/ maxQueueSize) * 100;
        if (percent > factor) {
            return false;
        }
        return true;
    }

    @Override
    public void addWaitQueue(String url) {
        cacheService.sAdd(NoteCacheKey.CRAWLER_WAIT_ENQUEUE_SET, url);
    }


    @Override
    public boolean support(String url) {
        return false;
    }

    public void add(String url) {
        Boolean isExist = cacheService.sIsMember(NoteCacheKey.CRAWLER_DUP_SET, url);
        Boolean isExist2 = cacheService.sIsMember(NoteCacheKey.CRAWLER_FAIL_SET, url);
        Boolean isExist3 = cacheService.sIsMember(NoteCacheKey.CRAWLER_SUCCESS_SET, url);
        Boolean isExist4 = cacheService.sIsMember(NoteCacheKey.CRAWLER_BLACKLIST_SET, url);
        Boolean isExist5 = cacheService.sIsMember(NoteCacheKey.CRAWLER_EMPTY_DATA_SET, url);
        if (!isExist && !isExist2 && !isExist3 && !isExist4 && !isExist5) {
            queue.offer(url);
            cacheService.sAdd(NoteCacheKey.CRAWLER_DUP_SET, url);
        }
    }

    @Override
    public void addFail(String url) {
        cacheService.sAdd(NoteCacheKey.CRAWLER_FAIL_SET, url);
        //从待爬取队列中删除
        cacheService.sRem(NoteCacheKey.CRAWLER_DUP_SET, url);
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
            //实现一个功能： 继续上次未完成的续爬
            //先获取到上次已完成的，然后将其从待爬的set中移除
            reDoLast();
            //开启一个分发线程
            log.info("==========启动deliveryTask===========");
            deliveryTask = new Thread(this);
            deliveryTask.start();
            log.info("==========启动deliveryTask Ok========");
        }
    }

    /**
     * 继续上次未完成的爬取
     */
    private void reDoLast() {
        //获取上次已成功爬取的
        //#bug20260129 发现随着数据量越来越大，读取和处理越来越慢，就放弃读取改success集合
        //Set<Object> successSet = cacheService.sMembers(NoteCacheKey.CRAWLER_SUCCESS_SET);
        //从上次待爬取集合中移除掉已完成的爬取任务
        /*for (Object ss : successSet) {
            cacheService.sRem(NoteCacheKey.CRAWLER_DUP_SET, ss);
        }
        successSet.clear();*/
        //排除中黑名单 url
       /* Set<Object> blackListSet = cacheService.sMembers(NoteCacheKey.CRAWLER_BLACKLIST_SET);
        for (Object ss : blackListSet) {
            cacheService.sRem(NoteCacheKey.CRAWLER_DUP_SET, ss);
        }
        blackListSet.clear();*/
        //将上次爬取失败的，重新进入爬取队列
        /*Set<Object> failsListSet = cacheService.sMembers(NoteCacheKey.CRAWLER_FAIL_SET);
        for (Object ss : failsListSet) {
            cacheService.sAdd(NoteCacheKey.CRAWLER_DUP_SET, ss);
        }
        failsListSet.clear();
        //清空上次失败集合
        cacheService.del(NoteCacheKey.CRAWLER_FAIL_SET);*/
        //获取上次本次待爬取任务，加入到队列中
        Set<Object> waitFetchSet = cacheService.sMembers(NoteCacheKey.CRAWLER_DUP_SET);
        if (waitFetchSet.size() > maxQueueSize) {
            queue = null;
            int curWaitSize = waitFetchSet.size();
            int allocNewSize = (int)(curWaitSize*1.2);
            log.info("当前默认队列太小，重新申请。 待处理量={}, 申请={}", curWaitSize, allocNewSize);
            queue = new ArrayBlockingQueue<>(allocNewSize);
            //重新赋值
            maxQueueSize = allocNewSize;
        } else {
            //new queue
            queue = new ArrayBlockingQueue<>(maxQueueSize);
        }
        //填充到待处理queue中
        for (Object wfs : waitFetchSet) {
            queue.offer(wfs.toString());
        }
        log.info("current queue size={}", queue.size());
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
            doWaitQueueEnqueue();
        }
    }

    private void doWaitQueueEnqueue() {
        long waitSize = cacheService.sCard(NoteCacheKey.CRAWLER_WAIT_ENQUEUE_SET);
        if (waitSize > 0L) {
            waitEnQueueFlg = true;
        } else {
            waitEnQueueFlg = false;
        }
        if (!waitEnQueueFlg) {
            return;
        }
        if (canEnqueue()) {
            //计算当前还有多少可用空间
            int availableSpaceSize = maxQueueSize - queueSize();
            float fac = 0.75f; //给与75%空间用于添加新url任务
            int allocSize = (int)(availableSpaceSize*fac);
            if (allocSize > waitSize) {
                allocSize = (int)waitSize;
            }
            List<Object> waitEnqueueUrls = cacheService.sPop(NoteCacheKey.CRAWLER_WAIT_ENQUEUE_SET, allocSize);
            for (Object waitEnqueueUrl : waitEnqueueUrls) {
                add(waitEnqueueUrl.toString());
            }
        }
        //todo 处理入队
    }
}

