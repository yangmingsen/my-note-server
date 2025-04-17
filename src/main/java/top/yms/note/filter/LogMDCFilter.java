package top.yms.note.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import top.yms.note.utils.IdWorker;

import javax.annotation.Resource;
import javax.servlet.*;
import java.io.IOException;

@Component
public class LogMDCFilter implements Filter {
    @Resource
    private IdWorker idWorker;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try{
            long requestId = idWorker.nextId();
            MDC.put("requestId", requestId+"");
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
