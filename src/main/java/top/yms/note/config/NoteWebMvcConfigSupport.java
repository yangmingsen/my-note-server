package top.yms.note.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import top.yms.note.converts.WebMvcConfigurationSupportConfigurer;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Component
public class NoteWebMvcConfigSupport extends WebMvcConfigurationSupportConfigurer {


//    @Value("${sys.mas-interceptor}")
    private String noteInterceptor = "true";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        if (noteInterceptor.equals("true")) {
            registry.addInterceptor(new NoteRequestInterceptor())
                    .addPathPatterns("/**")
                    .excludePathPatterns("/api/**","/actuator/**","/favicon.ico","/health","/account/login",
                            "/swagger-ui.html","/webjars/**","/swagger-resources/**");
        }

        super.addInterceptors(registry);
    }
}
