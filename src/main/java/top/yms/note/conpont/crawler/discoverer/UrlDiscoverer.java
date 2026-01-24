package top.yms.note.conpont.crawler.discoverer;

import java.util.Set;

@Deprecated
public interface UrlDiscoverer {

    boolean support(String pageUrl);

    Set<String> discover(String pageUrl) throws Exception;
}

