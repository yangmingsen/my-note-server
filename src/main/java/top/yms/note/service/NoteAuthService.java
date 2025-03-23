package top.yms.note.service;

import top.yms.note.dto.NoteAuth;
import top.yms.note.entity.RestOut;

public interface NoteAuthService {
    RestOut auth(NoteAuth noteAuth);
}
