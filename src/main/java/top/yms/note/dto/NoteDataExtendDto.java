package top.yms.note.dto;

import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteMeta;

import java.io.Serializable;
import java.util.List;

public class NoteDataExtendDto implements INoteData, Serializable {
    private Long userId;

    private NoteMeta noteMeta;

    private NoteData noteData;

    private List<NoteDataVersion> noteDataVersionList;

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public Long getId() {
        return noteMeta.getId();
    }

    @Override
    public String getContent() {
        return getNoteData().getContent();
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public void setContent(String content) {
        getNoteData().setContent(content);
    }

    @Override
    public NoteMeta getNoteIndex() {
        return noteMeta;
    }

    public void setNoteIndex(NoteMeta noteMeta) {
        this.noteMeta = noteMeta;
    }

    @Override
    public NoteData getNoteData() {
        return noteData;
    }

    public void setNoteData(NoteData noteData) {
        this.noteData = noteData;
    }

    @Override
    public List<NoteDataVersion> getNoteDataVersionList() {
        return noteDataVersionList;
    }

    public void setNoteDataVersionList(List<NoteDataVersion> noteDataVersionList) {
        this.noteDataVersionList = noteDataVersionList;
    }
}
