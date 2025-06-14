package top.yms.note.service;

import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteTag;

import java.util.List;

public interface NoteTagService {
    List<NoteTag> findByUser();

    Boolean addTag(NoteTag noteTag);

    List<NoteMeta> findByTag(Long tagId);
}
