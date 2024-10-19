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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteLuceneDataService;
import top.yms.note.conpont.NoteStoreService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteIndexMapper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Primary
@Component(NoteConstants.defaultNoteStoreServiceImpl)
public class DefaultNoteStoreServiceImpl implements NoteStoreService, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(DefaultNoteStoreServiceImpl.class);

    protected final List<NoteType> noteContentTypeList = new LinkedList<>();

    @Autowired
    protected NoteIndexMapper noteIndexMapper;

    @Override
    public Object findOne(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        for(NoteType noteType : noteContentTypeList) {
            if (noteType.support(noteIndex.getType())) {
                return noteType.getContent(id);
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
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
                return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
    }

    @Override
    public void update(Object note) {
        save(note);
    }


    protected NoteType findCanApplyNoteType(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        for(NoteType noteType : noteContentTypeList) {
            if (noteType.support(noteIndex.getType())) {
                return noteType;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        noteContentTypeList.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        context, NoteType.class, true, false).values());
        Collections.sort(noteContentTypeList);
        log.info("获取到NoteContentTypeList: {}", noteContentTypeList);
    }


}
