package top.yms.note.conpont.crawler.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ConcretePageNetworkNoteCrawler extends AbstractNetworkNoteCrawler{

    @Override
    List<String> urlDiscoverer(Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 runoob
            if (!href.startsWith("https://www.concretepage.com")) {
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
        Element titleEl = doc.selectFirst("body > div.mainBlock > div.bodyPart > div.leftMidPart > div.text > h1");
        return titleEl;
    }

    @Override
    Element matchContent(Document doc) {
//        Element contentEl = doc.selectFirst("div.bodyPart  div.leftMidPart  div.text");
        Element contentEl = doc.selectFirst("body > div.mainBlock > div.bodyPart > div.leftMidPart > div.text");
        return contentEl;
    }

    @Override
    public boolean support(String url) {
        return url.contains("www.concretepage.com");
    }

    @Override
    protected void processBeforeDocument(Document doc) {
        doc.select("div.date").remove();
        doc.select("body > div.mainBlock > div.bodyPart > div.leftMidPart > div.text > table:nth-child(3)").remove();
        doc.select("body > div.mainBlock > div.bodyPart > div.leftMidPart > div.text > table:nth-child(4)").remove();
        doc.select("div.brdCrumb").remove();
        doc.select("div.contentsTbl").remove();
        doc.select("table[border=0]").remove();
    }

    public String afterCompleted(String markdownStr) {
        int idx = markdownStr.indexOf("Reference {#ref}");
        if (idx < 0) {
            idx = markdownStr.indexOf("References {#ref}");
        }
        if (idx < 0) {
            idx = markdownStr.indexOf("Reference {#Reference}");
        }
        if (idx < 0) {
            idx = markdownStr.indexOf("References {#References}");
        }
        if (idx < 0) {
            idx = markdownStr.indexOf("### Reference");
        }
        idx = idx-8;
        if (idx < 0) {
            idx = markdownStr.indexOf("SIMILAR POSTS");
        }
        if (idx < 0) {
            idx = markdownStr.length();
        }
        return markdownStr.substring(0, idx);
    }

    protected Object getReferer(String url) {
        super.getReferer(url);
        crawlerTaskInfo.get().put(RefererFlg, "https://www.concretepage.com/");
        return "https://www.concretepage.com/";
    }

    public String imageUrlFilter(String imgUrl) {
        if (
                imgUrl.contains("images/youtube-subscribe.jpg") ||
                imgUrl.contains("author/images/arvind-rai-profile.png") ||
                imgUrl.contains("images/x.png") ||
                imgUrl.contains("images/facebook.png") ||
                imgUrl.contains("images/youtube.png")
        ) {
            return null;
        }
        return imgUrl;
    }

}
