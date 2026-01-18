package top.yms.note.conpont.crawler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.config.SpringContext;
import top.yms.note.conpont.SysConfigService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.crawler.discoverer.UrlDiscoverer;
import top.yms.note.conpont.crawler.impl.NetworkNoteCrawler;
import top.yms.note.conpont.crawler.limiter.CrawlerRateLimiter;
import top.yms.note.conpont.crawler.scheduler.DefaultUrlScheduler;
import top.yms.note.conpont.crawler.scheduler.UrlScheduler;
import top.yms.note.conpont.crawler.worker.CrawlWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Deprecated
public class CrawlerBootstrap {

    private final static Logger log = LoggerFactory.getLogger(CrawlerBootstrap.class);

    private static ExecutorService pool;

    public static void start() {
        log.info("=================Start Crawler==============");
        //del record
        NoteRedisCacheService noteRedisCacheService = SpringContext.getBean(NoteRedisCacheService.class);
        noteRedisCacheService.del(NoteCacheKey.CRAWLER_DUP_SET);
        //find target
        SysConfigService sysConfigService = SpringContext.getBean(SysConfigService.class);
        String sVal = sysConfigService.getStringValue("crawler.urls");
        String[] targetUrlArr = sVal.split(";");
        for (String targetUrl : targetUrlArr) {
            doStart(targetUrl);
        }
        log.info("=================Start Crawler Ok============");
    }

    private static void doStart(String targetUrl) {
        log.info("start crawler for:{}", targetUrl);
        UrlScheduler scheduler = SpringContext.getBean(DefaultUrlScheduler.class);
        scheduler.add(targetUrl);

        UrlDiscoverer discoverer = SpringContext.getBean(UrlDiscoverer.class);
        NetworkNoteCrawler articleCrawler = SpringContext.getBean(NetworkNoteCrawler.class);
        CrawlerRateLimiter rateLimiter = SpringContext.getBean(CrawlerRateLimiter.class);

        if (pool == null) {
            pool = Executors.newFixedThreadPool(3);
        }
        for (int i = 0; i < 3; i++) {
            pool.submit(new CrawlWorker(
                    scheduler,
                    articleCrawler,
                    discoverer,
                    rateLimiter
            ));
        }
    }
}
