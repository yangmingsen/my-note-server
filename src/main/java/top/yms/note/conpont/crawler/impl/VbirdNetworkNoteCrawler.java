package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class VbirdNetworkNoteCrawler extends AbstractNetworkNoteCrawler{
    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 wangdoc
            if (!href.startsWith("https://linux.vbird.org")) {
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
    Element matchTitle(Document doc) {
        Element titleEl = doc.selectFirst("body > div:nth-child(3) > header > h1");
        if (titleEl == null) {
            titleEl = doc.selectFirst("div.container header h1");
        }
        if (titleEl == null) {
            titleEl = doc.selectFirst("header h1");
        }
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("div.container div.row div.col-lg-9.col-xl-10");
        return contentEl;
    }

    @Override
    public boolean support(String url) {
        return url.contains("linux.vbird.org");
    }


    @Override
    protected void processBeforeDocument(Document doc) {
        doc.select("div.links").remove();
    }
}
