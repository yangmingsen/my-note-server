package top.yms.note.conpont.content;

import top.yms.note.conpont.ComponentSort;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

/**
 * 获取笔记内容抽象接口
 */
public interface NoteType extends  ComponentSort, Comparable<ComponentSort>{
    boolean support(String type);
    default Object getContent(Long id) {return null;}
    default Object getContent(NoteIndex noteIndex) {
        return null;
    }
    default void save(Object data) throws BusinessException {

    }
}
