package top.yms.note.dto;

import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteIndex;

import java.io.Serializable;
import java.util.List;

public class NoteDataExtendDto implements INoteData, Serializable {
    private Long userId;

    private NoteIndex noteIndex;

    private NoteData noteData;

    private List<NoteDataVersion> noteDataVersionList;

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public Long getId() {
        return noteIndex.getId();
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
        INoteData.super.setContent(content);
    }

    @Override
    public NoteIndex getNoteIndex() {
        return noteIndex;
    }

    public void setNoteIndex(NoteIndex noteIndex) {
        this.noteIndex = noteIndex;
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
