package top.yms.note.conpont.crawler.discoverer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RunoobUrlDiscoverer implements UrlDiscoverer {

    @Override
    public boolean support(String pageUrl) {
        return pageUrl.contains("runoob");
    }

    @Override
    public Set<String> discover(String pageUrl) throws Exception {
        Set<String> result = new HashSet<>();
        Document doc = Jsoup.connect(pageUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get();

        for (Element a : doc.select("a[href]")) {
            String href = a.absUrl("href");
            if (href == null || href.isEmpty()) {
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
            result.add(href);
        }

        return result;
    }
}
