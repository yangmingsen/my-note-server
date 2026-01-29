package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class WangDocNetworkNoteCrawler extends AbstractNetworkNoteCrawler{
    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 wangdoc
            if (!href.startsWith("https://wangdoc.com")) {
                continue;
            }
            //javaScript 不用爬
            if (href.contains("/javascript/")) {
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
    public boolean blackListMatch(String url) {
        return super.blackListMatch(url);
    }

    @Override
    Element matchTitle(Document doc) {
        Element titleEl = doc.selectFirst("body > section > div > div:nth-child(1) > div.column.is-8.is-6-widescreen.is-offset-1-widescreen > article > h1");
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("body > section > div > div:nth-child(1) > div.column.is-8.is-6-widescreen.is-offset-1-widescreen > article");
        return contentEl;
    }

    @Override
    public boolean support(String url) {
        return url.contains("wangdoc");
    }

    protected void processBeforeDocument(Document doc) {
        doc.select("div.page-meta").remove();
        doc.select("div.article-toc").remove();
        // 标题锚点清理（关键）
        doc.select("h1, h2, h3, h4, h5, h6").removeAttr("id");
        //删除标题里面的描点
        doc.select("h1 a, h2 a, h3 a, h4 a, h5 a, h6 a").remove();
    }
}
