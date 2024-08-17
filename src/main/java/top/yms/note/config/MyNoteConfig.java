package top.yms.note.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by yangmingsen on 2024/8/12.
 */
//@Configuration
//public class MyNoteConfig implements WebMvcConfigurer {
//    private static Logger log = LoggerFactory.getLogger(MyNoteConfig.class);
//    @Autowired
//    private NoteRequestInterceptor noteRequestInterceptor;
//
//    //将拦截器
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        log.info("addInterceptors..........");
//        registry.addInterceptor(noteRequestInterceptor)
//                .addPathPatterns("/**") //拦截所有的 url
//                .excludePathPatterns("/user/login")//排除url: /user/login (登录)
//                .excludePathPatterns("/user/reg") //排除url: /user/reg   (注册)
//                .excludePathPatterns("/image/**")//排除 image(图像) 文件夹下的所有文件
//                .excludePathPatterns("/**/*.js")//排除任意深度目录下的所有".js"文件
//                .excludePathPatterns("/**/*.css");
//    }
//
//}
