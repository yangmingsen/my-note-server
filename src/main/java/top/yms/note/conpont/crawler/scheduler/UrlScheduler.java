package top.yms.note.conpont.crawler.scheduler;

import top.yms.note.conpont.crawler.worker.CrawlWorkerQueue;

public interface UrlScheduler {

    boolean support(String url);

    void add(String url);

    /**
     * 记录失败请求
     * @param url
     */
    void addFail(String url);

    void addForRoot(String url);

    /**
     * 该方法 尽量不使用，因为使用了队列分发功能
     * @return
     */
    String take();

    boolean isEmpty();

    /**
     * 注册待分发队列
     * <p>必须在addForRoot之前添加，否则任务接不到queue中</p>
     * @param crawlWorkerQueue
     */
    void regCrawlWorkerQueue(CrawlWorkerQueue crawlWorkerQueue);

    void clear();
}
