package top.yms.note.service.impl;

import org.springframework.stereotype.Service;
import top.yms.note.entity.NoteMeta;
import top.yms.note.service.NoteStarsService;

import java.util.List;

@Service
public class NoteStarsServiceImpl implements NoteStarsService {
    @Override
    public List<NoteMeta> findByUser() {
        return null;
    }

    @Override
    public Boolean addStar(Long noteId) {
        return null;
    }
}
