package top.yms.note;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.yms.note.config.ConfigureListener;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@EnableTransactionManagement
@SpringBootApplication
@ComponentScan("top.yms")
public class MyNoteApplication {
    public static void main(String[] args) {
//        ConfigurableApplicationContext applicationContext = SpringApplication.run(MyNoteApplication.class, args);
//        Apps.show(applicationContext);
        SpringApplication app  = new SpringApplication(MyNoteApplication.class);
        app.addListeners(new ConfigureListener());
        app.run(args);

    }
}
