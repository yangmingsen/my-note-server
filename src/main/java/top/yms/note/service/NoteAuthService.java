package top.yms.note.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteCache;
import top.yms.note.dto.NoteAuth;
import top.yms.note.dto.NoteAuthPassword;
import top.yms.note.entity.NoteUser;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteUserMapper;
import top.yms.note.utils.JwtUtil;
import top.yms.note.vo.AuthResult;

/**
 * Created by yangmingsen on 2024/8/19.
 */
@Service
public class NoteAuthService {

    @Autowired
    NoteUserMapper noteUserMapper;

    @Autowired
    @Qualifier(Constants.defaultNoteCache)
    private NoteCache noteCache;

    @Autowired
    private JwtUtil jwtUtil;

    public RestOut auth(NoteAuth noteAuth) {
        if (noteAuth instanceof NoteAuthPassword) {
            NoteUser noteUser = noteUserMapper.selectByUserName(noteAuth.getUsername());
            if (noteUser == null) {
                return RestOut.failed("账号不存在");
            }
            if (noteUser.getPassword().equals(noteAuth.getPassword())) {
                String token = jwtUtil.generateToken(noteUser.getId().toString());
                noteCache.update(noteUser.getId().toString(), noteUser);
                AuthResult authResult = new AuthResult();
                authResult.setToken(token);
                authResult.setUserId(noteUser.getId());
                authResult.setUsername(noteUser.getUsername());
                authResult.setAvtarUrl(noteUser.getAvtar());
                return RestOut.success(authResult);
            }
            return RestOut.failed("密码不一致");
        }
        throw new BusinessException(CommonErrorCode.E_203007);
    }

}
