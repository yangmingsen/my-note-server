package top.yms.note.conpont.search;

import top.yms.note.conpont.ComponentComparable;
import top.yms.note.vo.SearchResult;

import java.util.List;

public interface SearchResultFilter extends ComponentComparable {
    /**
     * 过滤敏感内容
     * @param searchResults
     * @return
     */
    List<SearchResult> filter(List<SearchResult> searchResults);
}
