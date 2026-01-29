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
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.crawler.ImageUploader;
import top.yms.note.conpont.crawler.NetworkNoteStorageService;
import top.yms.note.conpont.crawler.scheduler.UrlScheduler;
import top.yms.note.conpont.crawler.util.DigestUtil;
import top.yms.note.entity.NetworkNote;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.IdWorker;

import javax.annotation.Resource;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

public abstract class AbstractNetworkNoteCrawler implements NetworkNoteCrawler{

    private static  final Logger log = LoggerFactory.getLogger(AbstractNetworkNoteCrawler.class);

    @Resource
    protected NetworkNoteStorageService networkNoteStorageService;

    @Resource
    protected ImageUploader imageUploader;

    @Resource
    protected IdWorker idWorker;

    @Resource
    protected NoteFileService noteFileService;

    @Resource
    protected UrlScheduler urlScheduler;

    @Resource
    protected NoteRedisCacheService cacheService;

    protected IdWorker getIdWorker() {
        return idWorker;
    }

    protected NoteFileService getNoteFileService() {
        return noteFileService;
    }


    /**
     * url 发现
     * @param doc
     */
    abstract List<String> urlDiscoverer(Document doc);

    /**
     * 黑名单匹配
     * @param url
     * @return true-中黑名单； false-未中
     */
    protected boolean blackListMatch(String url) {
        if (cacheService.sIsMember(NoteCacheKey.CRAWLER_BLACKLIST_SET, url)) {
            return true;
        }
        return false;
    }

    /**
     * 获取标题元素
     * @param doc
     * @return
     */
    abstract Element matchTitle(Document doc);

    /**
     * 获取内容元素
     * @param doc
     * @return
     */
    abstract Element matchContent(Document doc);

    private boolean isDataImage(String src) {
        return src != null && src.startsWith("data:image/");
    }

    private boolean isHttpImage(String src) {
        return src != null &&
                (src.startsWith("http://") || src.startsWith("https://"));
    }


    protected void matchImage(final long noteId, Element contentEl) {
        // 处理图片
        Elements images = contentEl.select("img");
        for (Element img : images) {
            //match img
            String imgUrl = img.absUrl("src");
            if (StringUtils.isBlank(imgUrl)) {
                continue;
            }
            if (imgUrl.startsWith("data:image")) {
                continue;
            }
            //找到格式。 可能链接后面没有后缀
            String suffix = "jpg";
            int idx = imgUrl.lastIndexOf('.');
            if (idx > 0 && idx < imgUrl.length() - 1) {
                suffix = imgUrl.substring(idx + 1);
            }
            //获取文件流，并上传
            try (InputStream in = new URL(imgUrl).openStream()) {
                String newUrl = imageUploader.upload(in, suffix, (noteFile) -> {
                    noteFile.setNoteRef(noteId);
                    getNoteFileService().add(noteFile);
                });
                img.attr("src", newUrl);
            } catch (Exception e) {
                // 失败直接跳过，不中断主流程
                log.error("transfer img error: {}", e.getMessage());
            }
        }
    }

    /**
     * 判断是否已经爬取过且成功的
     * @param
     * @return true-已爬取 ， false -未爬取
     */
    protected boolean successFetchDuplicateMatch(String md5Id) {
        //检查当前urL是否爬取过
        if (networkNoteStorageService.exists(md5Id)) {
            return true;
        }
        return false;
    }

    protected void addDiscovererUrlToQueue(List<String> urlDiscovererList) {
        for (String url : urlDiscovererList) {
            if (urlScheduler.canEnqueue()) {
                urlScheduler.add(url);
            } else {
                urlScheduler.addWaitQueue(url);
            }
        }
    }

    protected String afterCompleted(String markdownStr) {
        return markdownStr;
    }

    public NetworkNote crawl(String url) throws Exception {
        if (blackListMatch(url)) {
            return null;
        }
        String md5Id = DigestUtil.md5(url);
        if (successFetchDuplicateMatch(md5Id)) {
            return null;
        }
        Document doc = null;
        Connection.Response response = null;
        try {
            Connection connect = Jsoup.connect(url).proxy("127.0.0.1",10809);
            connect.header("User-Agent", UserAgentProvider.getUserAgent());
            Object oV = cacheService.sRandMember(NoteCacheKey.CRAWLER_DUP_SET);
            if (oV != null) {
                connect.header("Referer",oV.toString());
            }
            response = connect.timeout(5 * 1000).method(Connection.Method.GET).execute();
            doc = response.parse();
        } catch (org.jsoup.HttpStatusException hse) {
            log.error("http status error: {}", hse.getMessage());
            cacheService.sAdd(NoteCacheKey.CRAWLER_BLACKLIST_SET, url);
            return null;
        } catch (Throwable th) {
            log.error("connect {} error: {}", url, th.getMessage());
            urlScheduler.addFail(url);
            return null;
        }
        processBeforeDocument(doc);
        //do url discoverer
        List<String> urlDiscovererList = urlDiscoverer(doc);
        addDiscovererUrlToQueue(urlDiscovererList);
        //tile fetch
        Element titleEl = matchTitle(doc);
        //content fetch
        Element contentEl = matchContent(doc);
        //check it
        if (contentEl == null) {
            log.info("{} is empty data", url);
            return null;
        }

        final long noteId = getIdWorker().nextId();
        //image match
        matchImage(noteId, contentEl);
        //打包数据
        NetworkNote networkNote = new NetworkNote();
        networkNote.setMd5Id(md5Id);
        if (titleEl == null) {
            log.info("title is empty,random title");
            networkNote.setTitle("空标题(请重新命名)");
        } else {
            networkNote.setTitle(titleEl.text());
        }
        networkNote.setNoteId(noteId);
        //转换markdown
        String html = contentEl.html();
        if (StringUtils.isBlank(html)) {
            html = "<h1>空文档</h1>";
        }
        String markdownStr = afterCompleted(FlexmarkHtmlConverter.builder().build().convert(html));
        networkNote.setContent(markdownStr);
        networkNote.setUrl(url);
        networkNote.setCreateTime(new Date());
        return networkNote;
    }

    protected void processBeforeDocument(Document doc) {

    }

}
