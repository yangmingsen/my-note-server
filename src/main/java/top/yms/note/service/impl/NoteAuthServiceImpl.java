package top.yms.note.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.dto.NoteAuth;
import top.yms.note.dto.NoteAuthPassword;
import top.yms.note.entity.NoteUser;
import top.yms.note.entity.RestOut;
import top.yms.note.entity.UserLoginLog;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteUserMapper;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.repo.UserLoginLogRepository;
import top.yms.note.service.NoteAuthService;
import top.yms.note.utils.JwtUtil;
import top.yms.note.vo.AuthResult;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Created by yangmingsen on 2024/8/19.
 */
@Service
public class NoteAuthServiceImpl implements NoteAuthService {

    @Resource
    NoteUserMapper noteUserMapper;

    @Resource
    @Qualifier(NoteConstants.defaultNoteCache)
    private NoteCacheService noteCacheService;

    @Resource
    private UserLoginLogRepository userLoginLogRepository;

    @Resource
    private JwtUtil jwtUtil;

    public RestOut auth(NoteAuth noteAuth) {
        UserLoginLog userLoginLog = new UserLoginLog();
        userLoginLog.setLoginTime(new Date());
        userLoginLog.setUsername(noteAuth.getUsername());
        userLoginLog.setOtherInfo("login request");
        userLoginLogRepository.save(userLoginLog);
        //auth
        if (noteAuth instanceof NoteAuthPassword) {
            NoteUser noteUser = noteUserMapper.selectByUserName(noteAuth.getUsername());
            if (noteUser == null) {
                return RestOut.failed("账号不存在");
            }
            String srcStr = noteUser.getId().toString()+"_"+noteAuth.getPassword();
            String encryptedStr = DigestUtils.md5DigestAsHex(srcStr.getBytes(StandardCharsets.UTF_8));
            if (noteUser.getPassword().equals(encryptedStr)) {
                String token = jwtUtil.generateToken(noteUser.getId().toString());
                noteCacheService.update(noteUser.getId().toString(), noteUser);
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
