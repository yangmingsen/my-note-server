package top.yms.note.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import top.yms.note.conpont.NoteQueue;
import top.yms.note.conpont.task.NoteMemoryQueue;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.JwtUtil;

/**
 * Created by yangmingsen on 2024/8/18.
 */
@Configuration
public class NoteUtilConfig {

    @Bean
    public IdWorker idWorker() {
        return new IdWorker();
    }


    @Bean
    public RestTemplate createRestTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60*1000);//连接超时
        factory.setReadTimeout(60*1000);//读取超时

        return new RestTemplate(factory);
    }



    @Bean
    public NoteQueue noteLuceneIndexMemoryQueue() {
        return new NoteMemoryQueue();
    }
}
