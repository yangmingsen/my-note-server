package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class CnBlogsNetworkNoteCralwer extends AbstractNetworkNoteCrawler{
    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 cnblogs
            if (!href.startsWith("https://www.cnblogs.com")) {
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

    protected Object getReferer(String url) {
        super.getReferer(url);
        crawlerTaskInfo.get().put(RefererFlg, "https://www.cnblogs.com/");
        return "https://www.cnblogs.com/";
    }

    @Override
    public boolean blackListMatch(String url) {
        return super.blackListMatch(url);
    }

    @Override
    Element matchTitle(Document doc) {
        Element titleEl = doc.selectFirst("#topics > div > h1");
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("#topics > div > div.postBody");
        return contentEl;
    }

    @Override
    public boolean support(String url) {
        return url.contains("www.cnblogs.com");
    }

    protected void processBeforeDocument(Document doc) {
        doc.select("#blog_post_info_block").remove();
        doc.select("#MySignature").remove();
        // 标题锚点清理（关键）
        doc.select("h1, h2, h3, h4, h5, h6").removeAttr("id");
        //删除标题里面的描点
//        doc.select("h1 a, h2 a, h3 a, h4 a, h5 a, h6 a").remove();
    }

    @Override
    protected String afterCompleted(String markdownStr) {
        markdownStr = markdownStr.replaceAll("\\{#cnblogs_post_body}", "");
        return markdownStr;
    }
}
