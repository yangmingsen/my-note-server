package top.yms.note.conpont.content;

import top.yms.note.conpont.ComponentSort;
import top.yms.note.dto.INoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

/**
 * 笔记类型抽象接口
 */
public interface NoteType extends  ComponentSort, Comparable<ComponentSort>{
    /**
     * 是否支持当前类型(type)的内容获取或者保存
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
     * @param noteIndex
     * @return
     */
    default INoteData getContent(NoteIndex noteIndex) {
        return null;
    }

    /**
     * 支持当前类型的数据保存
     * @param iNoteData
     * @throws BusinessException
     */
    default void save(INoteData iNoteData) throws BusinessException {

    }

}
