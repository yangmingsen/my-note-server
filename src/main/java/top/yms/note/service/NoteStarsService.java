package top.yms.note.service;

import top.yms.note.entity.NoteIndex;

import java.util.List;

public interface NoteStarsService {
    List<NoteIndex> findByUser();

    Boolean addStar(Long noteId);
}
