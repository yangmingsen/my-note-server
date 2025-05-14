package top.yms.note.conpont;

import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;

import java.util.List;

/**
 * 用于从各种笔记类型中获取NoteLuceneIndex数据
 */
public interface NoteLuceneDataService {
    /**
     * 这个与 findNoteLuceneDataAll 方法功能一样，不过是获取指定ids的索引数据
     * @param ids
     * @return
     */
    default List<NoteLuceneIndex> findNoteLuceneDataList(List<Long> ids) {throw new BusinessException(CommonErrorCode.E_200214);}

    /**
     * 这个是获取某种笔记类型的所有索引数据。
     *  注意：如果是由NoteLuceneDataServiceImpl实现的，那应该是获取整个系统所有的笔记索引数据。
     *  这个暂时未实现
     * @return
     */
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

    default boolean supportGetEncryptDataForLucene() {return true;};

}
