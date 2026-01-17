package top.yms.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.yms.note.config.ConfigureListener;
import top.yms.note.conpont.chcek.CheckTargetTaskExecutorBootstrap;
import top.yms.note.other.CacheInit;

import java.util.Arrays;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@EnableTransactionManagement
@SpringBootApplication
@ComponentScan("top.yms")
public class MyNoteApplication {
    private final static Logger log = LoggerFactory.getLogger(MyNoteApplication.class);

    public static void main(String[] args) {
        log.info("MyNoteApplication args={}", Arrays.toString(args));
        SpringApplication app  = new SpringApplication(MyNoteApplication.class);
        app.addListeners(new ConfigureListener());
        app.run(args);
        //other exc
        otherStaticRun();
    }

    public static void otherStaticRun() {
        CheckTargetTaskExecutorBootstrap.start();
        //cache init
        //CacheInit.start();
    }
}
