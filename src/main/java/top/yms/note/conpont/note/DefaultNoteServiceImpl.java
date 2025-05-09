package top.yms.note.conpont.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteService;
import top.yms.note.dto.INoteData;
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
public class DefaultNoteServiceImpl implements NoteService, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(DefaultNoteServiceImpl.class);

    protected final List<Note> noteComponentList = new LinkedList<>();

    @Autowired
    protected NoteIndexMapper noteIndexMapper;

    @Override
    public void decryptNote(Long id) {
        NoteIndex noteMeta = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.support(noteMeta.getType())) {
                note.noteDecrypt(id);
                return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
    }

    @Override
    public void encryptNote(Long id) {
        NoteIndex noteMeta = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.support(noteMeta.getType())) {
                note.noteEncrypt(id);
                return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
    }

    private NoteIndex getNoteMeta(Long id) {
        NoteIndex noteMeta = noteIndexMapper.selectByPrimaryKey(id);
        if (noteMeta == null) {
            throw new BusinessException(CommonErrorCode.E_200201);
        }
        return noteMeta;
    }

    @Override
    public INoteData findOne(Long id) {
        NoteIndex noteMeta = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.support(noteMeta.getType())) {
                return note.getContent(id);
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
    }

    @Override
    public void save(INoteData iNoteData) {
        Long id = iNoteData.getId();
        NoteIndex noteIndex = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.support(noteIndex.getType())) {
                note.save(iNoteData);
                return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
    }

    @Override
    public void update(INoteData iNoteData) {
        save(iNoteData);
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        noteComponentList.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        context, Note.class, true, false).values());
        Collections.sort(noteComponentList);
        log.info("获取到NoteContentTypeList: {}", noteComponentList);
    }

    @Override
    public void destroy(Long id) {
        
    }

    @Override
    public String export(Long noteId, String exportType) {
        NoteIndex noteMeta = getNoteMeta(noteId);
        for(Note note : noteComponentList) {
            NoteExport noteExport = (NoteExport) note;
            if (noteExport.supportExport(noteMeta.getType(), exportType)) {
                return noteExport.export(noteId, exportType);
            }
        }
        throw new BusinessException(CommonErrorCode.E_200220);
    }
}
