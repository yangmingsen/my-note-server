package top.yms.note.conpont.crawler.limiter;

import org.springframework.stereotype.Component;

/**
 * 限速器（非常关键，防封）
 * <p>简单令牌桶（无需额外依赖）</p>
 */
public class SimpleRateLimiter implements CrawlerRateLimiter{

    private long intervalMillis = 1000;

    private long lastTime = 0;

    public SimpleRateLimiter(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    @Override
    public boolean support(String url) {
        return false;
    }

    public synchronized void acquire() {
        long now = System.currentTimeMillis();
        long wait = lastTime + intervalMillis - now;
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ignored) {
            }
        }
        lastTime = System.currentTimeMillis();
    }
}

