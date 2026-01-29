package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;

import java.util.LinkedList;
import java.util.List;


public class JuejinNetworkNoteCrawler extends AbstractNetworkNoteCrawler{


    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        Elements aHrefEls = doc.select("a[href]");
        for (Element a : aHrefEls) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            if (!href.contains("post")) {
                continue;
            }
            discoverList.add(href);
        }
        return discoverList;
    }

    @Override
    public boolean blackListMatch(String url) {
        if (cacheService.sIsMember(NoteCacheKey.CRAWLER_BLACKLIST_SET, url)) {
            return true;
        }
        return false;
    }

    @Override
    Element matchTitle(Document doc) {
        Element titleEl = doc.selectFirst("#juejin > div:nth-child(1) > div > main > div > div.main-area.article-area > article > h1");
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("#article-root");
        return contentEl;
    }

    @Override
    public boolean support(String url) {
        return url.contains("juejin");
    }
}
