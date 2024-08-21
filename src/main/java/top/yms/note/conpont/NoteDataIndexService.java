package top.yms.note.conpont;

import top.yms.note.dto.NoteDataIndex;

/**
 * 全文索引更新服务
 */
public interface NoteDataIndexService {
    /**
     * 更新数据索引
     * @param noteDataIndex
     */
    void update(NoteDataIndex noteDataIndex);
}
