package top.yms.note.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.entity.NoteUser;
import top.yms.note.service.NoteUserService;
import top.yms.note.utils.JwtUtil;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yangmingsen on 2024/8/19.
 */

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    @Qualifier(NoteConstants.defaultNoteCache)
    private NoteCacheService noteCacheService;

    @Resource
    private NoteUserService noteUserService;

    private static final Object syncObj = new Object();


    private NoteUser updateUserCache(String userId) {
        synchronized (syncObj) {
            NoteUser noteUser = (NoteUser) noteCacheService.find(userId);
            if (noteUser != null) {
                return noteUser;
            }

            noteUser = noteUserService.findOne(Long.parseLong(userId));
            noteCacheService.update(userId, noteUser);
            return noteUser;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            log.error("token is empty");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (jwtUtil.validateToken(token)) {
            String userId = jwtUtil.extractUserId(token);
            NoteUser noteUser = (NoteUser) noteCacheService.find(userId);
            if (noteUser == null) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                return false;

                //todo 暂时先以Token的过期时间为失效条件吧。 意味着如果缓存中没有了user的数据，那就重新查一下
                //todo 而不在以失效的方式
                noteUser = updateUserCache(userId);
            }
            Map<String, Object> localMap = LocalThreadUtils.get();
            localMap.put(userId, noteUser);
            localMap.put(NoteConstants.USER_ID, noteUser.getId());
            LocalThreadUtils.set(localMap);

            return true;
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LocalThreadUtils.remove();
    }
}

