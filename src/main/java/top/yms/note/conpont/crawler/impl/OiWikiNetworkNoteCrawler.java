package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;

import java.util.LinkedList;
import java.util.List;

@Component
public class OiWikiNetworkNoteCrawler extends AbstractNetworkNoteCrawler{
    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 runoob
            if (!href.startsWith("https://oi-wiki.org")) {
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
        Element titleEl = doc.selectFirst("body > div.md-container > main > div > div.md-content > article > h1");
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("body > div.md-container > main > div > div.md-content");
        return contentEl;
    }

    public void processBeforeDocument(Document doc) {
        // 1. 删除行号 td
        doc.select("td.linenos").remove();
        doc.select("a.headerlink").remove();
        doc.select("div.review-context-menu").remove();
        // 选中目标 table
        Elements tables = doc.select("table.highlighttable");
        for (Element table : tables) {
            table.tagName("div");
        }
        // 标题锚点清理（关键）
        doc.select("h1, h2, h3, h4, h5, h6").removeAttr("id");
        // 删除脚注 li 的 id（核心）
        doc.select("div.footnote li[id]")
                .removeAttr("id");
    }

    @Override
    public boolean support(String url) {
        return url.contains("oi-wiki");
    }
}
