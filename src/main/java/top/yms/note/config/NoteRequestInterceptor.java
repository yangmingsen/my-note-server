package top.yms.note.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import top.yms.note.comm.NoteConstants;
import top.yms.note.utils.LocalThreadUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yangmingsen on 2024/4/6.
 */
//@Component
public class NoteRequestInterceptor implements HandlerInterceptor {
    private static Logger log = LoggerFactory.getLogger(NoteRequestInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("from: addr={}", request.getRemoteAddr());
        Map<String, Object> map = LocalThreadUtils.get();
        map.put(NoteConstants.USER_ID, 1111L);
        LocalThreadUtils.set(map);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
