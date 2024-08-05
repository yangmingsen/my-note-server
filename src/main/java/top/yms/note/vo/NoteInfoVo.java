package top.yms.note.vo;

import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;

/**
 * Created by yangmingsen on 2024/8/6.
 */
public class NoteInfoVo {
    private NoteIndex noteIndex;

    //项目位置信息
    private NoteFile noteFile;

    public NoteIndex getNoteIndex() {
        return noteIndex;
    }

    public void setNoteIndex(NoteIndex noteIndex) {
        this.noteIndex = noteIndex;
    }

    public NoteFile getNoteFile() {
        return noteFile;
    }

    public void setNoteFile(NoteFile noteFile) {
        this.noteFile = noteFile;
    }

    @Override
    public String toString() {
        return "NoteInfoVo{" +
                "noteIndex=" + noteIndex +
                ", noteFile=" + noteFile +
                '}';
    }
}
