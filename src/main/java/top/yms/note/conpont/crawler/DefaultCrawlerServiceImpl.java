package top.yms.note.conpont.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.SysConfigService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.crawler.discoverer.UrlDiscoverer;
import top.yms.note.conpont.crawler.impl.NetworkNoteCrawler;
import top.yms.note.conpont.crawler.limiter.CrawlerRateLimiter;
import top.yms.note.conpont.crawler.limiter.SimpleRateLimiter;
import top.yms.note.conpont.crawler.scheduler.UrlScheduler;
import top.yms.note.conpont.crawler.worker.CrawlWorker;
import top.yms.note.conpont.crawler.worker.CrawlWorkerQueue;
import top.yms.note.conpont.crawler.worker.DefaultCrawlWorkerQueue;
import top.yms.note.entity.CrawlerTarget;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class DefaultCrawlerServiceImpl implements CrawlerService{

    private final static Logger log = LoggerFactory.getLogger(DefaultCrawlerServiceImpl.class);

    @Resource
    private NoteRedisCacheService cacheService;

    @Resource
    private UrlScheduler urlScheduler;

    @Resource
    private List<UrlDiscoverer> urlDiscovererList;

    @Resource
    private List<NetworkNoteCrawler> networkNoteCrawlerList;

    @Value("${crawler.per-crawler-worker-num}")
    private Integer perCrawlerWorkerNum;

    @Value("${crawler.status}")
    private Boolean crawlerStatus;

    private static ExecutorService executorService = null;

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            synchronized (this) {
                if (executorService != null) {
                    return executorService;
                }
                executorService = Executors.newCachedThreadPool();
            }
        }
        return executorService;
    }

    @Override
    public boolean support(Object condition) {
        return crawlerStatus;
    }

    @Override
    public void doCrawler() {
        log.info("=================Start Crawler==============");
        //urlScheduler clear
        urlScheduler.clear();
        //find target
        List<CrawlerTarget> crawlerTargetList = findAllCrawlerTargetList();
        if (crawlerTargetList.isEmpty()) {
            log.info("empty crawlerTargetList....");
            return;
        }
        //保存任务worker
        List<CrawlWorker> crawlWorkerList = new LinkedList<>();
        //foreach pack task
        for (CrawlerTarget crawlerTarget : crawlerTargetList) {
            //pack task
            CrawlWorker crawlWorker = new CrawlWorker();
            CrawlWorkerQueue crawlWorkerQueue = new DefaultCrawlWorkerQueue(crawlerTarget.getCondition());
            //注册任务queue
            urlScheduler.regCrawlWorkerQueue(crawlWorkerQueue);
            //target
            String targetUrl = crawlerTarget.getUrl();
            //foreach 加入一个根网址
            urlScheduler.addForRoot(targetUrl);
            //urlScheduler
            crawlWorker.setScheduler(urlScheduler);
            //crawlWorkerQueue
            crawlWorker.setCrawlWorkerQueue(crawlWorkerQueue);
            //urlDiscoverer
//            for (UrlDiscoverer urlDiscoverer : urlDiscovererList) {
//                if (urlDiscoverer.support(targetUrl)) {
//                    crawlWorker.setUrlDiscoverer(urlDiscoverer);
//                    break;
//                }
//            }
            //rateLimiter
            crawlWorker.setRateLimiter(new SimpleRateLimiter(1000));
            //networkNoteCrawler
            for (NetworkNoteCrawler networkNoteCrawler : networkNoteCrawlerList) {
                if (networkNoteCrawler.support(targetUrl)) {
                    crawlWorker.setNetworkNoteCrawler(networkNoteCrawler);
                    break;
                }
            }
            //pack
            crawlWorkerList.add(crawlWorker);
        }
        for (CrawlWorker crawlWorker : crawlWorkerList) {
            for (int i=0; i<perCrawlerWorkerNum; i++) {
                getExecutorService().submit(crawlWorker);
            }
        }
        log.info("=================Start Crawler Ok============");
    }

    private List<CrawlerTarget> findAllCrawlerTargetList() {
        List<CrawlerTarget> crawlerTargetList = new LinkedList<>();
        CrawlerTarget ct1 = new CrawlerTarget();
        ct1.setUrl("https://www.runoob.com/");
        ct1.setCondition("runoob");
        ct1.setOpen("1");
        crawlerTargetList.add(ct1);

        CrawlerTarget ct2 = new CrawlerTarget();
        ct2.setUrl("https://arthas.aliyun.com/doc/");
        ct2.setCondition("arthas.aliyun");
        ct2.setOpen("1");
        crawlerTargetList.add(ct2);

        CrawlerTarget ct3 = new CrawlerTarget();
        ct3.setUrl("https://oi-wiki.org/search/");
        ct3.setCondition("oi-wiki");
        ct3.setOpen("1");
        crawlerTargetList.add(ct3);

        return crawlerTargetList.stream().filter(ct -> ct.getOpen().equals("1")).collect(Collectors.toList());
    }
}
