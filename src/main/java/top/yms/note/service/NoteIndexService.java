package top.yms.note.service;

import top.yms.note.entity.NoteIndex;

public interface NoteIndexService {
    /**
     * 找到一个NoteIndex
     * @param id id
     * @return noteIndex
     */
    NoteIndex findOne(Long id);
}
