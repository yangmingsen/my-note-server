package top.yms.note.config;

/**
 * Created by yangmingsen on 2024/8/9.
 */
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import top.yms.note.comm.NoteConstants;

@Configuration
public class MongoConfig {
    @Value("${spring.data.mongodb.database}")
    String db;

//    @Bean
//    public GridFSBucket getGridFSBucket(MongoClient mongoClient){
//        MongoDatabase database = mongoClient.getDatabase(db);
//        GridFSBucket bucket = GridFSBuckets.create(database);
//        return bucket;
//    }

    @Bean(NoteConstants.legacyGridFsTemplate)
    public GridFsTemplate legacyGridFsTemplate(MongoDatabaseFactory dbFactory, MongoConverter converter) {
        return new GridFsTemplate(dbFactory, converter); // 默认 bucket: fs
    }

    @Bean(NoteConstants.legacyGridFSBucket) // 默认老bucket: fs
    public GridFSBucket legacyGridFSBucket(MongoClient mongoClient){
        MongoDatabase database = mongoClient.getDatabase(db);
        return GridFSBuckets.create(database, "fs"); // 原来的 bucket
    }


    @Bean(NoteConstants.bigFileGridFsTemplate)
    public GridFsTemplate bigFileGridFsTemplate(MongoDatabaseFactory dbFactory, MongoConverter converter) {
        return new GridFsTemplate(dbFactory, converter, "bigfile");
    }

    @Bean(NoteConstants.bigFileGridFsBucket) // 新 bucket: bigfile
    public GridFSBucket newGridFSBucket(MongoClient mongoClient){
        MongoDatabase database = mongoClient.getDatabase(db);
        return GridFSBuckets.create(database, "bigfile"); // 新 bucket
    }


}
