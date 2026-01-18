package top.yms.note.conpont.crawler.worker;

import java.util.concurrent.TimeUnit;

public interface CrawlWorkerQueue {

    /**
     * 判断当前url是否可以加入该任务队列
     * @param url
     * @return
     */
    boolean support(String url);

    /**
     * 获取一个待爬取url
     * @return
     */
    String take();

    /**
     * 获取数据，在指定时间内
     * @param timeout
     * @param unit
     * @return
     */
    String poll(long timeout, TimeUnit unit);

    /**
     * 向当前queue加入一个url
     * @param url
     */
    boolean offer(String url);

    void clear();

}
