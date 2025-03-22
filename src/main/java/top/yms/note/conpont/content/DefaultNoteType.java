package top.yms.note.conpont.content;

import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.conpont.NoteTikaService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.exception.BusinessException;

import java.io.InputStream;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class DefaultNoteType extends AbstractNoteType implements NoteTikaService {
    @Override
    public boolean support(String type) {
        return false;
    }

    @Override
    public Object doGetContent(Long id) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public void doSave(INoteData iNoteData) throws BusinessException {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    public boolean supportGetLuceneData(String type) {
        return true;
    }

    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteLuceneIndex noteLuceneIndex = packNoteIndexForNoteLuceneIndex(id);
        //todo 内容处理，后续需要调用tika获取内容
        noteLuceneIndex.setContent(streamToString(null));
        return noteLuceneIndex;
    }

    @Override
    public String streamToString(InputStream inputStream) {
        return null;
    }
}
