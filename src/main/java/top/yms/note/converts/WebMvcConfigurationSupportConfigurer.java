package top.yms.note.converts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.text.SimpleDateFormat;
import java.util.List;

//@Configuration
// @ConfigurationProperties(prefix = "spring.jackson")
public class WebMvcConfigurationSupportConfigurer extends WebMvcConfigurationSupport {
    // @Autowired
    // HandlerInterceptor httpInterceptor; // 定义时间格式转换器

    // private String dataFormat;


    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmss")); //输出 转换
        converter.setObjectMapper(mapper);
        return converter;
    } // 添加转换器


    public MappingJackson2HttpMessageConverter longToStringConverter() {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        jackson2HttpMessageConverter.setObjectMapper(objectMapper);

        return jackson2HttpMessageConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //
        // 将我们定义的时间格式转换器添加到转换器列表中,
        // 这样jackson格式化时候但凡遇到Date类型就会转换成我们定义的格式
        //
        //converters.add(jackson2HttpMessageConverter());
        converters.add(longToStringConverter());
    }



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加拦截器
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }


    @Override
    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
//       returnValueHandlers.add(new YCustomerHandlerMethodReturnValueHandler());
        super.addReturnValueHandlers(returnValueHandlers);

    }
}