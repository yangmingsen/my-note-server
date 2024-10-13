package top.yms.note.conpont.content;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteLuceneDataService;
import top.yms.note.conpont.search.NoteLuceneIndex;

import java.util.List;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component(NoteConstants.noteLuceneDataServiceImpl)
public class NoteLuceneDataServiceImpl extends DefaultNoteStoreServiceImpl implements NoteLuceneDataService {


    @Override
    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteType canApplyNoteType = findCanApplyNoteType(id);
        return ((NoteLuceneDataService) canApplyNoteType).findNoteLuceneDataOne(id);
    }
}
