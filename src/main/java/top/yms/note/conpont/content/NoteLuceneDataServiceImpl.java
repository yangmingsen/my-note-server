package top.yms.note.conpont.content;

import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteLuceneDataService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component(NoteConstants.noteLuceneDataServiceImpl)
public class NoteLuceneDataServiceImpl extends DefaultNoteStoreServiceImpl implements NoteLuceneDataService {

    /**
     * 这个服务不支持获取
      * @param type
     * @return
     */
    @Override
    public boolean supportGetLuceneData(String type) {
        return false;
    }

    /**
     * 获取能够支持获取lucene索引数据的noteType
     * @param id
     * @return
     */
    private NoteLuceneDataService findCanApplyNoteIndexNoteType(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        for(NoteType noteType : noteContentTypeList) {
            NoteLuceneDataService noteLuceneDataService = (NoteLuceneDataService) noteType;
            if (noteLuceneDataService.supportGetLuceneData(noteIndex.getType())) {
                return noteLuceneDataService;
            }
        }
        throw new BusinessException(CommonErrorCode.E_200215);
    }


    @Override
    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteLuceneDataService noteLuceneDataService = findCanApplyNoteIndexNoteType(id);
        return noteLuceneDataService.findNoteLuceneDataOne(id);
    }
}
