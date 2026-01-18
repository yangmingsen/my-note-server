package top.yms.note.conpont.crawler;

public interface CrawlerService {

    boolean support(Object condition);

    void doCrawler();
}
