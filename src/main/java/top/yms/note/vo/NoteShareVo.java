package top.yms.note.vo;

import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteShareInfo;

public class NoteShareVo {
    private NoteIndex noteIndex;
    private NoteData noteData;
    private NoteShareInfo noteShareInfo;

    public NoteIndex getNoteIndex() {
        return noteIndex;
    }

    public void setNoteIndex(NoteIndex noteIndex) {
        this.noteIndex = noteIndex;
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
