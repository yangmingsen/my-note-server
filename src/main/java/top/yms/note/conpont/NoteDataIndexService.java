package top.yms.note.conpont;

import top.yms.note.conpont.search.NoteDataIndex;
import top.yms.note.conpont.search.NoteLuceneIndex;

import java.util.List;

/**
 * 全文索引更新服务
 */
public interface NoteDataIndexService {
    /**
     * 更新数据索引
     * @param noteDataIndex
     */
    void update(NoteDataIndex noteDataIndex);

    void update(List<NoteLuceneIndex> noteLuceneIndexList);

    void updateByIds(List<Long> ids);

    void delete(Long id);

    void delete(List<Long> ids);

    void rebuildIndex();
}
