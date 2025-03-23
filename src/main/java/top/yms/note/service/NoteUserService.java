package top.yms.note.service;

import top.yms.note.entity.NoteUser;

public interface NoteUserService {
    NoteUser findOne(Long userId);
}
