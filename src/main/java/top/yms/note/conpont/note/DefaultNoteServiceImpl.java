package top.yms.note.conpont.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.NoteService;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.req.NoteShareReqDto;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteShareInfo;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.vo.NoteShareVo;

import javax.annotation.Resource;
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

    @Resource
    protected NoteMetaMapper noteMetaMapper;

    @Resource
    private NoteFileMapper noteFileMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    public void decryptNote(Long id) {
        NoteMeta noteMeta = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.support(noteMeta.getType())) {
                note.noteDecrypt(id);
                return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200224);
    }

    @Override
    public void encryptNote(Long id) {
        NoteMeta noteMeta = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.support(noteMeta.getType())) {
                note.noteEncrypt(id);
                return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200223);
    }

    private NoteMeta getNoteMeta(Long id) {
        NoteMeta noteMeta = noteMetaMapper.selectByPrimaryKey(id);
        if (noteMeta == null) {
            throw new BusinessException(CommonErrorCode.E_200201);
        }
        return noteMeta;
    }

    @Override
    public INoteData findOne(Long id) {
        NoteMeta noteMeta = getNoteMeta(id);
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
        NoteMeta noteMeta = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.support(noteMeta.getType())) {
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
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public String export(Long noteId, String exportType) {
        NoteMeta noteMeta = getNoteMeta(noteId);
        for(Note note : noteComponentList) {
            if (note.supportExport(noteMeta.getType(), exportType)) {
                log.debug("find note component => {}", note);
                return note.export(noteId, exportType);
            }
        }
        throw new BusinessException(CommonErrorCode.E_200220);
    }

    @Override
    public boolean supportDestroy(String noteType) {
        return false;
    }

    @Override
    public void noteDestroy(Long id) {
        NoteMeta noteMeta = getNoteMeta(id);
        for(Note note : noteComponentList) {
            if (note.supportDestroy(noteMeta.getType()) ) {
                log.debug("find note component => {}", note);
                 note.noteDestroy(id);
                 return;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200225);
    }

    @Override
    public NoteShareVo shareNoteGet(Long noteId) {
        NoteMeta noteMeta = getNoteMeta(noteId);
        for(Note note : noteComponentList) {
            if (note.supportShare(noteMeta.getType())) {
                NoteShareReqDto noteShareReqDto = new NoteShareReqDto();
                noteShareReqDto.setNoteIndex(noteMeta);
                return note.shareNoteGet(noteShareReqDto);
            }
        }
        throw new BusinessException(BusinessErrorCode.E_204013);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 10)
    @Override
    public void shareNoteClose(Long noteId) {
        NoteMeta noteMeta = getNoteMeta(noteId);
        for(Note note : noteComponentList) {
            if (note.supportShare(noteMeta.getType())) {
                NoteShareReqDto noteShareReqDto = new NoteShareReqDto();
                noteShareReqDto.setNoteIndex(noteMeta);
                note.shareNoteClose(noteShareReqDto);
                return;
            }
        }
        throw new BusinessException(BusinessErrorCode.E_204013);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 10)
    @Override
    public NoteShareInfo shareNoteOpen(Long noteId) {
        NoteMeta noteMeta = getNoteMeta(noteId);
        for(Note note : noteComponentList) {
            if (note.supportShare(noteMeta.getType())) {
                NoteShareReqDto noteShareReqDto = new NoteShareReqDto();
                noteShareReqDto.setNoteIndex(noteMeta);
                return note.shareNoteOpen(noteShareReqDto);
            }
        }
        throw new BusinessException(BusinessErrorCode.E_204013);
    }

    @Override
    public AnyFile shareResource(String id) {
        NoteFile noteFile = noteFileMapper.findOneByFileId(id);
        if (noteFile == null) {
            throw new BusinessException(BusinessErrorCode.E_204015);
        }
        Long noteId = noteFile.getNoteRef();
        if (noteId == 0L) {
            throw new BusinessException(BusinessErrorCode.E_204015);
        }
        //查看当前资源是否正在分享中
        NoteMeta noteMeta = noteMetaMapper.selectByPrimaryKey(noteId);
        if (NoteConstants.SHARE_UN_FLAG.equals(noteMeta.getShare())) {
            throw new BusinessException(BusinessErrorCode.E_204014);
        }
        //检查通过
        return fileStoreService.loadFile(id);
    }
}
