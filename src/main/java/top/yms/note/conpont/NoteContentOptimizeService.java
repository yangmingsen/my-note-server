package top.yms.note.conpont;

import java.util.List;

public interface NoteContentOptimizeService {

    /**
     * 去除掉不再需要的数据版本
     * @param id 笔记id
     * @return 删除的 ids
     */
    List<Long> removeOneUnnecessaryVersion(Long id);

    /**
     * 去除所有笔记不需要的版本
     */
    void removeAllUnnecessaryVersion();
}
