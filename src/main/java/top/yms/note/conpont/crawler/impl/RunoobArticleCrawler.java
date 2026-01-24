package top.yms.note.conpont.crawler.impl;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.crawler.NetworkNoteStorageService;
import top.yms.note.conpont.crawler.ImageUploader;
import top.yms.note.conpont.crawler.scheduler.UrlScheduler;
import top.yms.note.conpont.crawler.util.DigestUtil;
import top.yms.note.entity.NetworkNote;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.IdWorker;

import javax.annotation.Resource;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class RunoobArticleCrawler extends AbstractNetworkNoteCrawler{

    private static  final Logger log = LoggerFactory.getLogger(RunoobArticleCrawler.class);

    @Override
    public boolean support(String url) {
        return url.contains("runoob");
    }

    public List<String> urlDiscoverer (Document doc) {
        List<String> discoverList = new LinkedList<>();
        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (StringUtils.isBlank(href)) {
                continue;
            }
            // 只爬 runoob
            if (!href.startsWith("https://www.runoob.com/")) {
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

    public boolean blackListMatch(String url) {
        if (StringUtils.isBlank(url)) {
            return true;
        }
        if (url.contains("jdk11api") || url.contains("manual")) {
            cacheService.sAdd(NoteCacheKey.CRAWLER_BLACKLIST_SET, url);
            return true;
        }
        if (cacheService.sIsMember(NoteCacheKey.CRAWLER_BLACKLIST_SET, url)) {
            return true;
        }
        return false;
    }

    public Element matchTitle(Document doc) {
        Element titleEl = doc.selectFirst("#content > h1");
        if (titleEl == null) {
            titleEl = doc.selectFirst("body > div.container.main > div > div.col.middle-column.big-middle-column > div > div.article-heading > h2");
        }
        if (titleEl == null) {
            titleEl = doc.selectFirst("body > div.container.main > div.row > div.col.middle-column.big-middle-column > div > div.article-heading > h2");
        }
        return titleEl;
    }

     public Element matchContent(Document doc) {
        Element contentEl = doc.selectFirst("#content");
        if (contentEl == null) {
            contentEl = doc.selectFirst("body > div.container.main > div > div.col.middle-column.big-middle-column > div > div.article-body.note-body");
        }
        if (contentEl == null) {
            contentEl = doc.selectFirst("body > div.container.main > div.row > div.col.middle-column.big-middle-column > div > div.article-body.note-body > div");
        }
        if (contentEl == null) {
            contentEl = doc.selectFirst("body > div.container.main > div > div.col.middle-column.big-middle-column > div > div.article-body.note-body");
        }
        return contentEl;
    }

}

