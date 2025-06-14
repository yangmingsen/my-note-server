package top.yms.note.dto;


import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteMeta;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;

import java.util.List;

public interface INoteData {
    Long getId();

    String getContent();

    Long getUserId();

    /**
     * 设置当前笔记内容
     * @param content
     */
    default void setContent( String content) {}

    //*******  2025/05/10 后续应使用下面3个方法获取笔记数据  ******/

    /**
     * 获取笔记元数据
     * @return
     */
    default NoteMeta getNoteIndex() {throw new BusinessException(CommonErrorCode.E_200214);}

    /**
     * 获取笔记内容数据
     * @return
     */
    default NoteData getNoteData() {throw new BusinessException(CommonErrorCode.E_200214);}

    /**
     * 获取笔记内容版本数据
     * @return
     */
    default List<NoteDataVersion> getNoteDataVersionList() {throw new BusinessException(CommonErrorCode.E_200214);}
}
