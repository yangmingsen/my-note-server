package top.yms.note.conpont.crawler.limiter;

public interface CrawlerRateLimiter {

    boolean support(String url);

    void acquire();

}
