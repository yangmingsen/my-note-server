package top.yms.note.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteCache;
import top.yms.note.entity.NoteUser;
import top.yms.note.utils.JwtUtil;
import top.yms.note.utils.LocalThreadUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yangmingsen on 2024/8/19.
 */

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Qualifier(Constants.defaultNoteCache)
    private NoteCache noteCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            log.error("token is empty");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (jwtUtil.validateToken(token)) {
            String userId = jwtUtil.extractUsername(token);
            NoteUser noteUser = (NoteUser)noteCache.find(userId);
            if (noteUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            Map<String, Object> localMap = LocalThreadUtils.get();
            localMap.put(userId, noteUser);
            localMap.put(Constants.USER_ID, noteUser.getId());
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

