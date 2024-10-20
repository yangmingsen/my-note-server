package top.yms.note.conpont;

import top.yms.note.comm.CommonErrorCode;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.exception.BusinessException;

import java.util.List;

/**
 * 用于获取NoteLuceneIndex数据
 */
public interface NoteLuceneDataService {
    default List<NoteLuceneIndex> findNoteLuceneDataList(List<Long> ids) {throw new BusinessException(CommonErrorCode.E_200214);}

    default List<NoteLuceneIndex> findNoteLuceneDataAll() {throw new BusinessException(CommonErrorCode.E_200214);}

    /**
     * 根据noteId获取当前笔记的索引数据
     * @param id noteId
     * @return
     */
    NoteLuceneIndex findNoteLuceneDataOne(Long id);

    /**
     * 核验当前笔记类型是否支持获取索引数据
     * @param type 笔记类型（如 md,pdf,wer...)
     * @return true表示支持，否则false-
     */
    boolean supportGetLuceneData(String type);

}
