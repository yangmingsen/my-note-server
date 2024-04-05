package top.yms.note.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by yangmingsen on 2022/9/19.
 */
public class Apps {
    private static final Logger logger = LoggerFactory.getLogger(Apps.class);

    public static void show(ApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        String port = env.getProperty("server.port");
        String name = env.getProperty("spring.application.name");
        String path = env.getProperty("server.servlet.context-path");

        if (StringUtils.isBlank(path)) {
            path = "";
        }
        String ip = "127.0.0.1";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();
            if (StringUtils.isNotBlank(hostAddress)) {
                ip = hostAddress;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String info = "\n----------------------------------------------------------\n\t" +
                name.toUpperCase() + " is running! Access URLs:\n\t" +
                "Local: \t\thttp://" + ip + ":" + port + path + "/\n\t" +
                "swagger-ui: \thttp://" + ip + ":" + port + path + "/swagger-ui.html\n\t" +
                "actuator: \thttp://" + ip + ":" + port + path + "/actuator/info\n\t\n" +
                "----------------------------------------------------------";

        logger.info(info);
        
    }
}
