package top.yms.note.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.entity.NoteData;
import top.yms.note.mapper.NoteDataMapper;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Service
public class NoteDataService {

    private static Logger log = LoggerFactory.getLogger(NoteDataService.class);

    @Autowired
    private NoteDataMapper noteDataMapper;

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
    public void addAndUpdate(NoteData noteData) {
        Long id = noteData.getId();
        NoteData dbNote = noteDataMapper.findById(id);
        if (dbNote == null) {
            noteDataMapper.insert(noteData);
        } else {
            noteDataMapper.updateByPrimaryKeySelective(noteData);
        }

    }

    public NoteData get(Long id) {
        return noteDataMapper.selectByPrimaryKey(id);
    }
}
