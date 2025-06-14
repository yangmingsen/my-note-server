package top.yms.note.vo;

import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteShareInfo;

public class NoteShareVo {
    private NoteMeta noteMeta;
    private NoteData noteData;
    private NoteShareInfo noteShareInfo;

    public NoteMeta getNoteIndex() {
        return noteMeta;
    }

    public void setNoteIndex(NoteMeta noteMeta) {
        this.noteMeta = noteMeta;
    }

    public NoteData getNoteData() {
        return noteData;
    }

    public void setNoteData(NoteData noteData) {
        this.noteData = noteData;
    }

    public NoteShareInfo getNoteShareInfo() {
        return noteShareInfo;
    }

    public void setNoteShareInfo(NoteShareInfo noteShareInfo) {
        this.noteShareInfo = noteShareInfo;
    }
}
