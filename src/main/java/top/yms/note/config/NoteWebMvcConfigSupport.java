package top.yms.note.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import top.yms.note.converts.WebMvcConfigurationSupportConfigurer;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Configuration
public class NoteWebMvcConfigSupport extends WebMvcConfigurationSupportConfigurer {

    private static Logger log = LoggerFactory.getLogger(NoteWebMvcConfigSupport.class);
    @Value("${sys.note-interceptor}")
    private String noteInterceptor;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("执行添加拦截器....");
        if (noteInterceptor.equals("true")) {
            log.info("执行添加拦截器....Ok");
            registry.addInterceptor(jwtInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/user/login","/file/view",
                            "/actuator/**","/favicon.ico","/health","/account/login",
                            "/swagger-ui.html","/webjars/**","/swagger-resources/**");
        }

        super.addInterceptors(registry);
    }
}
