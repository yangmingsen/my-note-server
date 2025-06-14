package top.yms.note.service;

import top.yms.note.entity.NoteMeta;

import java.util.List;

public interface NoteStarsService {
    List<NoteMeta> findByUser();

    Boolean addStar(Long noteId);
}
