package top.yms.note.conpont;

import top.yms.note.entity.NoteIndex;

import java.util.List;

public interface NoteRecentVisitService {
    /**
     * 获取最近访问列表
     * @return
     */
    List<NoteIndex> getRecentVisitList();

    List<NoteIndex> getRecentVisitList(Long userId);
}
