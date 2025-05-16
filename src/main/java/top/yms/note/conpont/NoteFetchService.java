package top.yms.note.conpont;

public interface NoteFetchService {
    Long fetch(String url, String toType, Long parentId);
}
