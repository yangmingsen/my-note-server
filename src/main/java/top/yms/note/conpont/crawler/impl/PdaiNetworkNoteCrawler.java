package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class PdaiNetworkNoteCrawler extends AbstractNetworkNoteCrawler{
    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 runoob
            if (!href.startsWith("https://pdai.tech")) {
                continue;
            }
            // 只要文章页
            if (!href.endsWith(".html")) {
                continue;
            }
            //暂时注释
            if (href.contains("x-interview.h") || href.contains("x-interview-2.h")) {
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
        Element titleEl = doc.selectFirst("#app > div > main > div.theme-default-content > div > h1");
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("div.theme-default-content");
        return contentEl;
    }

    @Override
    protected boolean blackListMatch(String url) {
        boolean p = super.blackListMatch(url);
        if (p) {
            return p;
        }
        if (url.endsWith("resource/tools.html")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean support(String url) {
        return url.contains("pdai.tech");
    }

    protected void processBeforeDocument(Document doc) {
        // 标题锚点清理（关键）
        doc.select("h1, h2, h3, h4, h5, h6").removeAttr("id");
        //删除标题里面的描点
        doc.select("h1 a, h2 a, h3 a, h4 a, h5 a, h6 a").remove();
        //
        doc.select("#app > div > main > div.theme-default-content > div > ul:nth-child(3)").remove();
        //删除代码行号
        doc.select("div.line-numbers").remove();
    }
}
