package top.yms.note;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.service.NoteIndexService;
import top.yms.note.utils.Apps;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@EnableTransactionManagement
@SpringBootApplication
public class MyNoteApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MyNoteApplication.class, args);
        Apps.show(applicationContext);
//
//        NoteIndexService noteIndexService = applicationContext.getBean(NoteIndexService.class);
//        System.out.println(noteIndexService.mockDataFromDir());
//

//        NoteIndexMapper bean = applicationContext.getBean(NoteIndexMapper.class);
//        long [] arr = {2,3,4,5,6};
//        List<Long> ids = Arrays.stream(arr).boxed().collect(Collectors.toList());
//        bean.delByListIds(ids);
//
//        System.exit(0);



    }
}
