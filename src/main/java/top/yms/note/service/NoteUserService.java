package top.yms.note.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.yms.note.entity.NoteUser;
import top.yms.note.mapper.NoteUserMapper;

/**
 * Created by yangmingsen on 2024/10/1.
 */
@Service
public class NoteUserService {

    @Autowired
    private NoteUserMapper noteUserMapper;


    public NoteUser findOne(Long userId) {
        return noteUserMapper.selectByPrimaryKey(userId);
    }
}
