package top.yms.note.conpont.crawler.worker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DefaultCrawlWorkerQueue implements CrawlWorkerQueue{

    private final static Logger log = LoggerFactory.getLogger(DefaultCrawlWorkerQueue.class);

    private final String condition;

    private final int queueSize = 3000;

    private final ArrayBlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(queueSize);

    public DefaultCrawlWorkerQueue(String condition) {
        this.condition = condition;
    }

    @Override
    public boolean support(String url) {
        return url.contains(condition);
    }

    @Override
    public String take() {
        try {
            return blockingQueue.take();
        } catch (InterruptedException e) {
            log.error("take error: {}", e.getMessage());
            return null;
        }
    }

    public String poll(long timeout, TimeUnit unit) {
        try {
            return blockingQueue.poll(timeout, unit);
        } catch (InterruptedException e) {
            log.error("poll with timeout error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean offer(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return blockingQueue.offer(url);
    }

    @Override
    public void clear() {
        blockingQueue.clear();
    }
}
