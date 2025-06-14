package top.yms.note.conpont.note;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteLuceneDataService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.entity.NoteMeta;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@Component(NoteConstants.noteLuceneDataServiceImpl)
public class NoteLuceneDataServiceImpl extends DefaultNoteServiceImpl implements NoteLuceneDataService {

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
        NoteMeta noteMeta = noteMetaMapper.selectByPrimaryKey(id);
        for(Note note : noteComponentList) {
            NoteLuceneDataService noteLuceneDataService = (NoteLuceneDataService) note;
            if (noteLuceneDataService.supportGetLuceneData(noteMeta.getType())) {
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
