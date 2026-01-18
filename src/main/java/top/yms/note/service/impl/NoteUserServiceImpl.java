package top.yms.note.service.impl;

import org.springframework.stereotype.Service;
import top.yms.note.conpont.SysConfigService;
import top.yms.note.entity.NoteUser;
import top.yms.note.mapper.NoteUserMapper;
import top.yms.note.service.NoteUserService;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;

/**
 * Created by yangmingsen on 2024/10/1.
 */
@Service
public class NoteUserServiceImpl implements NoteUserService {

    @Resource
    private NoteUserMapper noteUserMapper;

    @Resource
    private SysConfigService sysConfigService;


    public NoteUser findOne(Long userId) {
        return noteUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public Long getUserId() {
        //获取userId
        Long userId = LocalThreadUtils.getUserId();
        if (userId == null) {
            userId = sysConfigService.getLongValue("sys.default-user-id");
        }
        return userId;
    }
}
