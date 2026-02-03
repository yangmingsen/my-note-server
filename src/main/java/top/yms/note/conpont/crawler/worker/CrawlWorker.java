package top.yms.note.conpont.crawler.worker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.note.config.SpringContext;
import top.yms.note.conpont.crawler.discoverer.UrlDiscoverer;
import top.yms.note.conpont.crawler.impl.CrawlerTargetMessage;
import top.yms.note.conpont.crawler.impl.NetworkNoteCrawler;
import top.yms.note.conpont.crawler.impl.NetworkNoteMessage;
import top.yms.note.conpont.crawler.limiter.CrawlerRateLimiter;
import top.yms.note.conpont.crawler.scheduler.UrlScheduler;
import top.yms.note.conpont.queue.ProducerService;
import top.yms.note.conpont.task.NoteTask;
import top.yms.note.entity.CrawlerTarget;
import top.yms.note.entity.NetworkNote;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 并发 Worker（核心调度）
 *
 * 固定线程池
 * 所有线程 共享一个 RateLimiter
 * 发现 URL + 爬文章一起做
 */
public class CrawlWorker implements NoteTask {

    private String name;

    private static  final Logger log = LoggerFactory.getLogger(CrawlWorker.class);

    private  UrlScheduler scheduler;

    private  NetworkNoteCrawler networkNoteCrawler;

    private  UrlDiscoverer urlDiscoverer;

    private  CrawlerRateLimiter rateLimiter;

    private CrawlerTarget crawlerTarget;

    /**
     * 待处理任务队列
     */
    private CrawlWorkerQueue crawlWorkerQueue;

    private ProducerService producerService;

    private ProducerService getProducerService() {
        if (producerService == null) {
            producerService = SpringContext.getBean(ProducerService.class);
        }
        return producerService;
    }

    public void setScheduler(UrlScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setNetworkNoteCrawler(NetworkNoteCrawler networkNoteCrawler) {
        this.networkNoteCrawler = networkNoteCrawler;
    }

    public CrawlerTarget getCrawlerTarget() {
        return crawlerTarget;
    }

    public void setCrawlerTarget(CrawlerTarget crawlerTarget) {
        this.crawlerTarget = crawlerTarget;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public void setUrlDiscoverer(UrlDiscoverer urlDiscoverer) {
        this.urlDiscoverer = urlDiscoverer;
    }

    public void setRateLimiter(CrawlerRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public void setCrawlWorkerQueue(CrawlWorkerQueue crawlWorkerQueue) {
        this.crawlWorkerQueue = crawlWorkerQueue;
    }

    public CrawlWorker(UrlScheduler scheduler,
                       NetworkNoteCrawler networkNoteCrawler,
                       UrlDiscoverer urlDiscoverer,
                       CrawlerRateLimiter rateLimiter) {
        this.scheduler = scheduler;
        this.urlDiscoverer = urlDiscoverer;
        this.rateLimiter = rateLimiter;
        this.networkNoteCrawler = networkNoteCrawler;
    }
    public CrawlWorker() {}

    @Override
    public void run() {
        if (scheduler == null ) {
            log.error("scheduler must not null");
            return;
        }
        if (networkNoteCrawler == null ) {
            log.error("networkNoteCrawler must not null");
            return;
        }
//        if (urlDiscoverer == null ) {
//            log.error("urlDiscoverer must not null");
//            return;
//        }
        if (rateLimiter == null ) {
            log.error("rateLimiter must not null");
            return;
        }
        if (crawlWorkerQueue == null ) {
            log.error("crawlWorkerQueue must not null");
            return;
        }
        byte emptyCnt = 0;
        while (true) {
            try {
                String url = crawlWorkerQueue.poll(3, TimeUnit.SECONDS);
                if (StringUtils.isBlank(url)) {
                    emptyCnt++;
                    if (emptyCnt > 5) { //如果15内没有新任务，认为是没有数据了，该退出
                        log.info("{}, 15s内无任务退出", this);
                        //通知完成
                        CrawlerTargetMessage crawlerTargetMessage = new CrawlerTargetMessage();
                        crawlerTargetMessage.setBody(crawlerTarget);
                        getProducerService().send(crawlerTargetMessage);
                        break;
                    }
                    continue;
                }
                //重置
                emptyCnt=0;
                // 全局限速
                rateLimiter.acquire();
                // 2. 爬文章
                long startTime = System.currentTimeMillis();
                NetworkNote networkNote = networkNoteCrawler.crawl(url);
                if (networkNote != null) {
                    // 这里你可以直接入库
                    long endTime = System.currentTimeMillis();
                    log.info("Tid={}, 爬取成功：{} , 耗时：{} s" , Thread.currentThread().getName(), networkNote.getTitle(), (endTime-startTime)/1000);
                    NetworkNoteMessage message = new NetworkNoteMessage();
                    message.setBody(networkNote);
                    getProducerService().send(message);
                }
            } catch (Exception e) {
                // 记录日志，继续跑
                log.error("CrawlWorker Error: {}", e.getMessage());
            }
        }
    }
}

