package top.yms.note.conpont.note;

import top.yms.note.conpont.ComponentComparable;
import top.yms.note.dto.INoteData;
import top.yms.note.entity.NoteMeta;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;

/**
 * 笔记抽象接口
 */
public interface Note extends NoteExport, NoteEncrypt, NoteVersion,
        NoteDestroy, ComponentComparable, NoteShare {
    /**
     * 是否支持当前类型(type)的内容
     * @param type
     * @return
     */
    boolean support(String type);

    /**
     * 根据noteId获取内容
     * @param id
     * @return
     */
    default INoteData getContent(Long id) {return null;}

    /**
     * 根据noteIdex对象获取，暂时未实现。 请用 getContent(id)
     * @param noteMeta
     * @return
     */
    default INoteData getContent(NoteMeta noteMeta) {
        return null;
    }

    /**
     * 支持当前类型的数据保存
     * @param iNoteData
     * @throws BusinessException
     */
    default void save(INoteData iNoteData) throws BusinessException {

    }

    /**
     * 是否支持数据保存
     * @return true-是 , - false
     */
    boolean supportSave();

    /**
     * 获取当前笔记占用空间（B-字节)
     * @return size byte
     */
    default long getNoteSize() {throw new BusinessException(CommonErrorCode.E_200211);}

}
