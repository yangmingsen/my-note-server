package top.yms.note.conpont.note;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteTikaService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class DefaultNote extends AbstractNote implements NoteTikaService {

    @Value("${other.share-support-type}")
    private String otherShareSupport;

    private Set<String> shareSupportSet;

    private Set<String> getShareSupportSet() {
        if (shareSupportSet == null) {
            shareSupportSet = new HashSet<>();
            shareSupportSet.addAll(Arrays.asList(otherShareSupport.split(",")));
        }
        return shareSupportSet;
    }

    @Override
    public boolean support(String type) {
        return true;
    }

    @Override
    public INoteData doGetContent(Long id) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public void doSave(INoteData iNoteData) throws BusinessException {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    public int getSortValue() {
        return 999;
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

//    public boolean supportShare(String type) {
//        return getShareSupportSet().contains(type);
//    }
}
