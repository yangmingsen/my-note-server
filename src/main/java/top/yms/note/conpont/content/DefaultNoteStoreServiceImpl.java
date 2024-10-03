package top.yms.note.conpont.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteStoreService;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.mapper.NoteIndexMapper;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class DefaultNoteStoreServiceImpl implements NoteStoreService, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(DefaultNoteStoreServiceImpl.class);

    private final List<NoteType> noteContentTypeList = new LinkedList<>();

    @Autowired
    private NoteIndexMapper noteIndexMapper;

    @Override
    public Object findOne(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        for(NoteType noteType : noteContentTypeList) {
            if (noteType.support(noteIndex.getType())) {
                return noteType.getContent(id);
            }
        }
        log.error("未找到id:{}的笔记", id);
        return null;
    }

    @Override
    public void save(Object note) {
        Long id = null;
        if (note instanceof NoteDataDto) {
            id = ((NoteDataDto) note).getId();
        }
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        for(NoteType noteType : noteContentTypeList) {
            if (noteType.support(noteIndex.getType())) {
                noteType.save(note);
                break;
            }
        }

    }

    @Override
    public void update(Object note) {
        save(note);
    }

//    private boolean init = false;
//    @Override
//    public void onApplicationEvent(ApplicationEvent event) {
//        if (!init && event.getSource() instanceof AnnotationConfigServletWebServerApplicationContext) {
//            ApplicationContext context = (ApplicationContext) event.getSource();
//            noteContentTypeList.addAll(
//                BeanFactoryUtils.beansOfTypeIncludingAncestors(
//                        context, NoteType.class, true, false).values());
//            log.info("noteContentTypeList: {}", noteContentTypeList);
//        }
//
//    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        noteContentTypeList.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        context, NoteType.class, true, false).values());
        log.info("获取到NoteContentTypeList: {}", noteContentTypeList);
    }
}
