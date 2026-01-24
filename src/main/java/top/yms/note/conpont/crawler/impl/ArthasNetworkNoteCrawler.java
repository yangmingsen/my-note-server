package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;

import java.util.LinkedList;
import java.util.List;

@Component
public class ArthasNetworkNoteCrawler extends AbstractNetworkNoteCrawler{
    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 runoob
            if (!href.startsWith("https://arthas.aliyun")) {
                continue;
            }
            // 只要文章页
            if (!href.endsWith(".html")) {
                continue;
            }
            // 过滤明显非文章
            if (href.contains("/try/") || href.contains("#")) {
                continue;
            }
            discoverList.add(href);
        }
        return discoverList;
    }

    @Override
    boolean blackListMatch(String url) {
        if (cacheService.sIsMember(NoteCacheKey.CRAWLER_BLACKLIST_SET, url)) {
            return true;
        }
        return false;
    }

    @Override
    Element matchTitle(Document doc) {
        Element titleEl = doc.selectFirst("div.theme-default-content h1");;
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("#app > div > main > div > div:nth-child(2)");
        return contentEl;
    }

    protected void processBeforeDocument(Document doc) {
        doc.select("h1, h2, h3, h4, h5, h6")
                .removeAttr("id");
        doc.select("a.header-anchor").remove();
    }

    @Override
    public boolean support(String url) {
        return url.contains("arthas.aliyun");
    }
}
