package top.yms.note.conpont.fetch;

import top.yms.note.conpont.ComponentComparable;


public interface NoteFetch extends ComponentComparable {

    boolean supportFetch(String type);

    Long fetch(String url, Long parentId);

    Long fetch(AbstractNoteFetch.FetchMeta fetchMeta);
}
