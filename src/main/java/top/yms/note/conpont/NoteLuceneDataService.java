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

    NoteLuceneIndex findNoteLuceneDataOne(Long id);

}
