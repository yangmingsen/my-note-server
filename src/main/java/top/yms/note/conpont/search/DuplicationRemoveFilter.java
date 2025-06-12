package top.yms.note.conpont.search;

import org.springframework.stereotype.Component;
import top.yms.note.vo.SearchResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 重复内容过滤器
 */
@Component
public class DuplicationRemoveFilter implements SearchResultFilter {
    @Override
    public List<SearchResult> filter(List<SearchResult> searchResults) {
        Set<Long> idSets = new HashSet<>();
        List<SearchResult> resList = new LinkedList<>();
        for (SearchResult searchResult : searchResults) {
            Long noteId = searchResult.getId();
            if (idSets.contains(noteId)) {
                continue;
            }
            idSets.add(noteId);
            resList.add(searchResult);
        }
        return resList;
    }

    @Override
    public int getSortValue() {
        return 30;
    }
}
