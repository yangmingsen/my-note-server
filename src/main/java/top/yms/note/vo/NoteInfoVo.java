package top.yms.note.vo;

import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteMeta;

/**
 * Created by yangmingsen on 2024/8/6.
 */
public class NoteInfoVo {
    private NoteMeta noteMeta;

    //项目位置信息
    private NoteFile noteFile;

    public NoteMeta getNoteIndex() {
        return noteMeta;
    }

    public void setNoteIndex(NoteMeta noteMeta) {
        this.noteMeta = noteMeta;
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
                "noteIndex=" + noteMeta +
                ", noteFile=" + noteFile +
                '}';
    }
}
