package top.yms.note.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;


/**
 * Created by yangmingsen on 2024/10/3.
 */
public class ConfigureListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private final static Logger log = LoggerFactory.getLogger(ConfigureListener.class);
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // 获取spring Environment
        MutablePropertySources propertySources = event.getEnvironment().getPropertySources();
        // 配置放在了application-pro或者是application-dev 中 赋值复制需要在其中赋值

        Properties myProperties  = new Properties();
        PropertiesPropertySource myPropertiesPropertySource = new PropertiesPropertySource("noteConig",
                myProperties
                );

        for (PropertySource<?> propertySource : propertySources) {
            boolean applicationConfig = propertySource.getName().contains("application");
            if (!applicationConfig) {
                continue;
            }

            // 获取上文的application集合中获取数据库连接
            Map<String, OriginTrackedValue> dataBaseSource =
                    (Map<String, OriginTrackedValue>)propertySource.getSource();

//            String driverClass = String.valueOf(dataBaseSource.get("spring.datasource.driver-class-name").getValue());
            String url = String.valueOf(dataBaseSource.get("spring.datasource.url").getValue());
            String user = String.valueOf(dataBaseSource.get("spring.datasource.username").getValue());
            String password = String.valueOf(dataBaseSource.get("spring.datasource.password").getValue());

            // 因为在spring初始化之前 所有不能使用注解 所以需要jdbc直接连接数据库 首先建立驱动
            Connection conn = null;
            Statement st = null;
            ResultSet rs = null;
            try {
                conn = DriverManager.getConnection(url, user, password);
                // 1、获取连接对象
                // 2、创建statement类对象，用来执行SQL语句！！
                st = conn.createStatement();
                // 3、创建sql查询语句
                String sql = "select * from t_system_config";
                // 4、执行sql语句并且换回一个查询的结果集
                rs = st.executeQuery(sql);
                log.debug("--------GetConfigFromDB--------------");
                while (rs.next()) {
                    // 获取数据库中的数据
                    String configKey = rs.getString("f_config_key");
                    String configValue = rs.getString("f_config_value");
                    log.debug("configKey: {} => value: {}", configKey, configValue);
                    // 通过数据库中的配置 修改application集合中数据
                    Map<String, OriginTrackedValue> source = (Map<String, OriginTrackedValue>)propertySource.getSource();
                    OriginTrackedValue originTrackedValue = source.get(configKey);

                    if (originTrackedValue == null) {
                        myProperties.put(configKey, configValue);
                    }

                }
                log.debug("--------GetConfigFromDB Over--------------");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    rs.close();
                    st.close();
                    conn.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        propertySources.addLast(myPropertiesPropertySource);

    }
}
