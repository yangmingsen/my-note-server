package top.yms.note.conpont;

import top.yms.note.conpont.fetch.AbstractNoteFetch;

public interface NoteFetchService {

    Long fetch(String url, String toType, Long parentId);

    Long fetch(AbstractNoteFetch.FetchMeta fetchMeta, String toType);
}
