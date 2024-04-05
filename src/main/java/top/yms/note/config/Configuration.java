package top.yms.note.config;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.springframework.context.annotation.Bean;
import top.yms.note.utils.IdWorker;

import javax.sql.DataSource;

/**
 * Created by yangmingsen on 2024/4/3.
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public IdWorker idWorker() {
        return new IdWorker();
    }
}
