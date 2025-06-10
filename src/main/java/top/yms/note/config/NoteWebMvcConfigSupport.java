package top.yms.note.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import top.yms.note.converts.WebMvcConfigurationSupportConfigurer;

import javax.annotation.Resource;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Configuration
public class NoteWebMvcConfigSupport extends WebMvcConfigurationSupportConfigurer {

    private static Logger log = LoggerFactory.getLogger(NoteWebMvcConfigSupport.class);
    @Value("${sys.note-interceptor:true}")
    private String noteInterceptor;

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (noteInterceptor.equals("true")) {
            log.debug("添加Jwt拦截器....Ok");
            registry.addInterceptor(jwtInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/user/login","/file/view","/file/download",
                            "/file/tmpView",
                            "/share/**",
                            "/actuator/**","/favicon.ico","/health","/account/login",
                            "/swagger-ui.html","/webjars/**","/swagger-resources/**");
        }

        super.addInterceptors(registry);
    }
}
