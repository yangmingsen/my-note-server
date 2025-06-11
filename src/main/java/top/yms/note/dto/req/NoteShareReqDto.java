package top.yms.note.dto.req;

import top.yms.note.entity.NoteIndex;

public class NoteShareReqDto {
    private NoteIndex noteIndex;

    public NoteIndex getNoteIndex() {
        return noteIndex;
    }

    public void setNoteIndex(NoteIndex noteIndex) {
        this.noteIndex = noteIndex;
    }
}
