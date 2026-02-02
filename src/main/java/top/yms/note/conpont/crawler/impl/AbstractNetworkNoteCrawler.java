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
import org.springframework.beans.factory.annotation.Value;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.crawler.ImageUploader;
import top.yms.note.conpont.crawler.NetworkNoteStorageService;
import top.yms.note.conpont.crawler.limiter.CrawlerRateLimiter;
import top.yms.note.conpont.crawler.limiter.SimpleRateLimiter;
import top.yms.note.conpont.crawler.scheduler.UrlScheduler;
import top.yms.note.conpont.crawler.util.DigestUtil;
import top.yms.note.entity.NetworkNote;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.IdWorker;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Value("${crawler.file-upload-async}")
    private boolean fileUploadAsync;

    protected final static ThreadLocal<Map<String, Object>> crawlerTaskInfo = new ThreadLocal<>();

    protected final static String RefererFlg = "RefererFlg";

    private CrawlerRateLimiter crawlerRateLimiter = new SimpleRateLimiter(1000);


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

    private InputStream openImageStream(String imgUrl) throws IOException {
        URL url = new URL(imgUrl);
        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection(ProxyFactory.http());
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);
        conn.setRequestProperty("User-Agent", UserAgentProvider.getUserAgent());
        conn.setRequestProperty("Accept",
                "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");
        Map<String, Object> thMap = crawlerTaskInfo.get();
        if (thMap != null) {
            Object refUrl = thMap.get(RefererFlg);
            if (refUrl != null) {
                conn.setRequestProperty("Referer", refUrl.toString());
            }
        }
        conn.setInstanceFollowRedirects(true);

        return conn.getInputStream();
    }


    protected String imageUrlFilter(String imgUrl) {
        return imgUrl;
    }


    protected void matchImage(final long noteId, Element contentEl) {
        // 处理图片
        Elements images = contentEl.select("img");
        for (Element img : images) {
            //match img
            String imgUrl = img.absUrl("src");
            imgUrl = imageUrlFilter(imgUrl);
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
            if (fileUploadAsync) {
                try {
                    String newUrl = imageUploader.asyncUpload(imgUrl, suffix, (noteFile) -> {
                        noteFile.setNoteRef(noteId);
                        getNoteFileService().add(noteFile);
                    });
                    img.attr("src", newUrl);
                } catch (Exception e) {
                    // 失败直接跳过，不中断主流程
                    log.error("fetch image error: {}", e.getMessage());
                }
            } else {
                //开启限速
                crawlerRateLimiter.acquire();
                //获取文件流，并上传
                try (InputStream in = openImageStream(imgUrl)) {
                    String newUrl = imageUploader.upload(in, suffix, (noteFile) -> {
                        noteFile.setNoteRef(noteId);
                        getNoteFileService().add(noteFile);
                    });
                    img.attr("src", newUrl);
                    log.info("fetch image success, url={}", newUrl);
                } catch (Exception e) {
                    // 失败直接跳过，不中断主流程
                    log.error("fetch image error: {}", e.getMessage());
                }
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

    protected Object getReferer(String url) {
        //将加入到
        Map<String, Object> thMap = crawlerTaskInfo.get();
        if (thMap == null) {
            thMap = new HashMap<>();
            crawlerTaskInfo.set(thMap);
        }
        thMap.put(RefererFlg, url);
        return url;
    }

    public NetworkNote crawl(String url) throws Exception {
        if (blackListMatch(url)) {
            //若中黑名单，从待爬取队列中删除
            cacheService.sRem(NoteCacheKey.CRAWLER_DUP_SET, url);
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
            Object oV = getReferer(url);
            if (oV != null) {
                connect.header("Referer",oV.toString());
            }
            response = connect.timeout(30 * 1000).method(Connection.Method.GET).execute();
            doc = response.parse();
        } catch (org.jsoup.HttpStatusException hse) {
            log.error("http status error: {}", hse.getMessage());
            cacheService.sAdd(NoteCacheKey.CRAWLER_BLACKLIST_SET, url);
            return null;
        } catch (Throwable th) {
            log.error("fetch url={} error: {}", url, th.getMessage());
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
            //将抓到的空数据记录下来
            cacheService.sAdd(NoteCacheKey.CRAWLER_EMPTY_DATA_SET, url);
            cacheService.sRem(NoteCacheKey.CRAWLER_DUP_SET, url);
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
