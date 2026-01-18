package top.yms.note.conpont.crawler.impl;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.config.SpringContext;
import top.yms.note.conpont.crawler.NetworkNoteStorageService;
import top.yms.note.conpont.crawler.ImageUploader;
import top.yms.note.conpont.crawler.util.DigestUtil;
import top.yms.note.conpont.queue.ProducerService;
import top.yms.note.entity.NetworkNote;
import top.yms.note.entity.NoteFile;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.IdWorker;

import javax.annotation.Resource;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Component
public class RunoobArticleCrawler implements NetworkNoteCrawler{
    private static  final Logger log = LoggerFactory.getLogger(RunoobArticleCrawler.class);

    @Resource
    private  NetworkNoteStorageService networkNoteStorageService;

    @Resource
    private  ImageUploader imageUploader;

    @Resource
    private IdWorker idWorker;

    @Resource
    private NoteFileService noteFileService;

    private IdWorker getIdWorker() {
        return idWorker;
    }

    private NoteFileService getNoteFileService() {
        return noteFileService;
    }

    @Override
    public boolean support(String url) {
        return url.contains("runoob");
    }

    public NetworkNote crawl(String url) throws Exception {
        //检查当前urL是否爬取过
        String md5Id = DigestUtil.md5(url);
        if (networkNoteStorageService.exists(md5Id)) {
            return null;
        }
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get();
        //tile fetch
        Element titleEl = doc.selectFirst("#content > h1");
        //content fetch
        Element contentEl = doc.selectFirst("#content");
        //check it
        if (titleEl == null || contentEl == null) {
            return null;
        }
        final long noteId = getIdWorker().nextId();
        // 处理图片
        Elements images = contentEl.select("img");
        for (Element img : images) {
            //match img
            String imgUrl = img.absUrl("src");
            if (imgUrl == null || imgUrl.isEmpty()) {
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
        //打包数据
        NetworkNote networkNote = new NetworkNote();
        networkNote.setMd5Id(md5Id);
        networkNote.setTitle(titleEl.text());
        networkNote.setNoteId(noteId);
        //转换markdown
        String html = contentEl.html();
        if (StringUtils.isBlank(html)) {
            html = "<h1>空文档</h1>";
        }
        String markdownStr = FlexmarkHtmlConverter.builder().build().convert(html);
        networkNote.setContent(markdownStr);
        networkNote.setUrl(url);
        networkNote.setCreateTime(new Date());
        return networkNote;
    }
}

