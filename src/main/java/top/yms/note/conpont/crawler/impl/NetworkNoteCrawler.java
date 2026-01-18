package top.yms.note.conpont.crawler.impl;

import top.yms.note.entity.NetworkNote;

public interface NetworkNoteCrawler {

    boolean support(String url);

    NetworkNote crawl(String url) throws Exception;
}
