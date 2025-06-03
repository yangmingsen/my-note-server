package top.yms.note.conpont.fetch;

import top.yms.note.conpont.ComponentSort;

public interface NoteFetch extends ComponentSort, Comparable<ComponentSort>{

    boolean supportFetch(String type);

    Long fetch(String url, Long parentId);

    Long fetch(AbstractNoteFetch.FetchMeta fetchMeta);
}
