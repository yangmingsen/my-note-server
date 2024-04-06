package top.yms.note.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import top.yms.note.utils.IdWorker;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/4/3.
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public IdWorker idWorker() {
        return new IdWorker();
    }


    public MongoClient mongoClient() {
        //连接到MongoDB服务 如果是远程连接可以替换“localhost”为服务器所在IP地址
        //ServerAddress()两个参数分别为 服务器地址 和 端口
        ServerAddress serverAddress = new ServerAddress("106.12.253.7",9430);
        List<ServerAddress> addrs = new ArrayList<ServerAddress>();
        addrs.add(serverAddress);

        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
        MongoCredential credential =
                MongoCredential.createScramSha1Credential("forp_whzq", "forp_whzq", "DuNgoNuVc234".toCharArray());
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        credentials.add(credential);

        //通过连接认证获取MongoDB连接
        MongoClient mongoClient = new MongoClient(addrs,credentials);

        return mongoClient;
    }

}
