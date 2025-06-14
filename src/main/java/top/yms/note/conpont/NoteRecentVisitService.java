package top.yms.note.conpont;

import top.yms.note.entity.NoteMeta;

import java.util.List;

public interface NoteRecentVisitService {
    /**
     * 获取最近访问列表
     * @return
     */
    List<NoteMeta> getRecentVisitList();

    List<NoteMeta> getRecentVisitList(Long userId);

    /**
     *从lruCache中删除某个笔记。比如在某个笔记被删除后，不应该再出现在lru列表中
     * @param id
     */
    default void remove(Long id, Long userId) {}
}
