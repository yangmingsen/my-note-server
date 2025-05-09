package top.yms.note.conpont.note;

import top.yms.note.dto.INoteData;

public interface NoteVersion {
    /**
     * 是否支持数据版本
     * @return true 支持 , false 不支持
     */
    boolean supportVersion();

    void addNoteVersion(INoteData iNoteData);
}
