package top.yms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import top.yms.note.config.SpringContext;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteIndexService;

import javax.annotation.Resource;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootApplicationTests {

    @Resource
    private NoteDataService noteDataService;

    @Test
    public void testGet() {

    }
}
