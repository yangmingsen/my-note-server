package top.yms.note.dto.req;

import top.yms.note.entity.NoteMeta;

public class NoteShareReqDto {
    private NoteMeta noteMeta;

    public NoteMeta getNoteIndex() {
        return noteMeta;
    }

    public void setNoteIndex(NoteMeta noteMeta) {
        this.noteMeta = noteMeta;
    }
}
