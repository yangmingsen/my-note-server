package top.yms.note.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import top.yms.note.utils.HostIPUtil;

import java.util.Arrays;

@Configuration
public class GlobalCorsConfig {
    private static final Logger logger = LoggerFactory.getLogger(GlobalCorsConfig.class);

    @Value("${cors.allowed-hosts}")
    private String allowedHosts;

    @Value("${cors.allowed-ports}")
    private String allowedPorts;


    @Value("${cors.allowed-method}")
    private String allowedMethod;

    private static String SP = ",";


    private String getAllowedHosts() {
        String hosts = this.allowedHosts;
        String localIP = HostIPUtil.getLocalIP();
        hosts = hosts+","+localIP;
        return hosts;
    }


    /**
     * 组装 host+port
     * @param allowedHosts
     * @param allowedPorts
     * @return
     */
    private String [] getAllowedOrigins(String allowedHosts, String allowedPorts) {
        String[] hosts = getArray(allowedHosts);
        String[] ports = getArray(allowedPorts);
        int len = hosts.length;

        int totalSize = len+len*ports.length;

        String [] res = new String[totalSize];
        int idx = 0;
        for (String s : hosts) {
            res[idx++] = "http://" + s;
        }
        for (String host : hosts) {
            for (String port : ports) {
                res[idx] = "http://" + host + ":" + port;
                idx++;
            }
        }

        logger.info("AllowedOrigins => "+ Arrays.toString(res));

        return res;
    }

    private String []  getArray(String str) {
        return str.split(SP);
    }

    @Bean
    public CorsFilter corsFilter() {
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //1) 允许的域,不要写*，否则cookie就无法使用了
        //注意：必须要和端口一起加上
        //config.addAllowedOrigin("http://127.0.0.1:3000");
        //config.addAllowedOrigin("http://localhost:3000");
        for( String origin : getAllowedOrigins(getAllowedHosts(), allowedPorts)) {
            config.addAllowedOrigin(origin);
        }



        //2) 是否发送Cookie信息
        config.setAllowCredentials(true);

        //3) 允许的请求方式
        for( String method : getArray(allowedMethod)) {
            config.addAllowedMethod(method);
        }

        // 4）允许的头信息
        config.addAllowedHeader("*");

        //2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);

        //3.返回新的CorsFilter.
        return new CorsFilter(configSource);
    }
}