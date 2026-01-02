package top.yms.note.conpont.search;

import top.yms.note.vo.SearchResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 重复内容过滤器
 */
//@Component //bug20250613 暂时停用
public class DuplicationRemoveFilter implements SearchResultFilter {

    /**
     * 过滤掉重复
     * @param searchResults
     * @return
     */
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
