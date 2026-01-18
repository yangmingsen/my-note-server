package top.yms.note.conpont.sync;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteStoreService;
import top.yms.note.entity.NetworkNote;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteMeta;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.repo.NetworkNoteRepository;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteMetaService;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Deprecated
@Component
public class NetworkNoteSyncServiceImpl implements NoteSyncService{

    @Resource
    private NetworkNoteRepository networkNoteRepository;

    @Resource
    private NoteMetaService noteMetaService;

    @Resource
    private NoteDataService noteDataService;

    @Override
    public boolean support() {
        return true;
    }

    private Long getParentId() {
        return 1831555224603525121L;
    }

    @Override
    public void doSync() {
        Long userId = LocalThreadUtils.getUserId();
        for (NetworkNote networkNote : networkNoteRepository.findAll()) {
            Long noteId = networkNote.getNoteId();
            NoteMeta oVal = noteMetaService.findOne(noteId);

            NoteMeta noteMeta = new NoteMeta();
            noteMeta.setId(networkNote.getNoteId());
            noteMeta.setName(networkNote.getTitle());
            noteMeta.setType(FileTypeEnum.MARKDOWN.getValue());
            noteMeta.setParentId(getParentId());
            noteMeta.setStoreSite(NoteConstants.MYSQL);
            noteMeta.setUserId(userId);
            noteMeta.setIsFile(NoteConstants.FILE_FLAG);
            Date cDate = new Date();
            noteMeta.setCreateTime(cDate);
            noteMeta.setUpdateTime(cDate);
            noteMeta.setSize((long)networkNote.getContent().getBytes(StandardCharsets.UTF_8).length);

            //prepare data
            NoteData noteData = new NoteData();
            noteData.setId(noteId);
            noteData.setUserId(userId);
            noteData.setContent(networkNote.getContent());
            noteData.setCreateTime(cDate);
            noteData.setUpdateTime(cDate);
            if (oVal == null) {
                //add to db
                noteMetaService.add(noteMeta);
                //add to db
                noteDataService.save(noteData);
            } else {
                noteMetaService.update(noteMeta);
                noteDataService.save(noteData);
            }
        }
    }
}
