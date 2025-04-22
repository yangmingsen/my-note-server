package top.yms.note.conpont.search;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.SensitiveService;
import top.yms.note.vo.SearchResult;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class EncryptedContentFilter implements SearchContentFilter {

    @Resource
    private SensitiveService sensitiveService;

    @Override
    public List<SearchResult> filter(List<SearchResult> searchResults) {
        List<SearchResult> filterRes = new LinkedList<>();
        for (SearchResult searchResult : searchResults) {
            if (!hit(searchResult)) {
                filterRes.add(searchResult);
            }
        }
        return filterRes;
    }

    /**
     * 是否击中
     * @param searchResult
     * @return true 击中
     */
    private boolean hit(SearchResult searchResult) {
        Long id = searchResult.getId();
        return sensitiveService.isSensitive(id);
    }


}
