package top.yms.note.service.impl;

import org.springframework.stereotype.Service;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteTag;
import top.yms.note.service.NoteTagService;

import java.util.List;

@Service
public class NoteTagServiceImpl implements NoteTagService {
    @Override
    public List<NoteTag> findByUser() {
        return null;
    }

    @Override
    public Boolean addTag(NoteTag noteTag) {
        return null;
    }

    @Override
    public List<NoteIndex> findByTag(Long tagId) {
        return null;
    }
}
