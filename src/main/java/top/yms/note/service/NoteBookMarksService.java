package top.yms.note.service;

public interface NoteBookMarksService {
    void syncWithLocalBookmarks() throws Exception;

    void syncBookmarksNote() throws Exception;
}
