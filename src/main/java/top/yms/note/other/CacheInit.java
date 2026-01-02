package top.yms.note.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.config.SpringContext;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.service.NoteMetaService;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CacheInit {

    private static final Logger log = LoggerFactory.getLogger(CacheInit.class);

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Resource
    private NoteMetaService noteMetaService;

    private void init() {
        List<Long> idList = noteMetaMapper.findAllById();
        log.info("init idList size={}", idList.size());
        for (Long id : idList) {
            noteMetaService.findOne(id);
        }
    }

    public static void start() {
        CacheInit cacheInit = SpringContext.getBean(CacheInit.class);
        cacheInit.init();
    }
}
