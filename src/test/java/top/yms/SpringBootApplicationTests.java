package top.yms;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.yms.note.service.NoteDataService;

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
