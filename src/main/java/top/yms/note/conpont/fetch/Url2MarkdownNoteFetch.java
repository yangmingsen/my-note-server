package top.yms.note.conpont.fetch;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.BusinessErrorCode;

@Component
public class Url2MarkdownNoteFetch extends AbstractNoteFetch {

    private static  final Logger log = LoggerFactory.getLogger(Url2MarkdownNoteFetch.class);

    @Value("${note.fetch.md-prefix}")
    private String fetchMdPrefix;

    @Override
    public boolean supportFetch(String type) {
        return toType().equals(type);
    }

    @Override
    String toType() {
        return NoteConstants.markdownSuffix;
    }

    @Override
    Long doFetch(FetchMeta fetchMeta) {
        Long id = fetchMeta.getNoteIndex().getId();
        String requrl = getFetchReqHost()+fetchMdPrefix+fetchMeta.getUrl();
        log.debug("Url2MarkdownNoteFetch reqUrl={}", requrl);
        String html = restTemplate.getForObject(requrl, String.class);
        if (StringUtils.isBlank(html)) {
            throw new BusinessException(BusinessErrorCode.E_204010);
        }
        String title = Jsoup.parse(html).title();
        log.debug("get title = {}", title);
        fetchMeta.getNoteIndex().setName(title);
        //转换markdown
        String markdown = FlexmarkHtmlConverter.builder().build().convert(html);
        fetchMeta.setContent(markdown);
        return id;
    }
}
