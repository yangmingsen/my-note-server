package top.yms.note.conpont.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.vo.SearchResult;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchResultSortFilter implements SearchResultFilter{

    private static  final Logger log = LoggerFactory.getLogger(SearchResultSortFilter.class);

    @Override
    public List<SearchResult> filter(List<SearchResult> searchResults) {
        List<SearchResult> resList = searchResults.stream().sorted((x, y) -> y.getResType() - x.getResType()).collect(Collectors.toList());
        log.debug("sortList = {}", resList.stream().map(SearchResult::getResType).collect(Collectors.toList()));
        return resList;
    }

    @Override
    public int getSortValue() {
        return 1;
    }
}
