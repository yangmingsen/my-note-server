package top.yms.note.conpont.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.yms.note.vo.SearchResult;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
@Primary
public class SearchResultCompositeFilter implements SearchResultFilter, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(SearchResultCompositeFilter.class);

    private final List<SearchResultFilter> searchResultFilters = new LinkedList<>();

    @Override
    public List<SearchResult> filter(List<SearchResult> searchResults) {
        for (SearchResultFilter searchResultFilter : searchResultFilters) {
            searchResults = searchResultFilter.filter(searchResults);
        }
        return searchResults;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Collection<SearchResultFilter> containerValues = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                context, SearchResultFilter.class, true, false).values();
        for (SearchResultFilter containerValue : containerValues) {
            if (containerValue != this) {
                searchResultFilters.add(containerValue);
            }
        }
        Collections.sort(searchResultFilters);
        log.info("SearchResultFilter={}", searchResultFilters);
    }
}
