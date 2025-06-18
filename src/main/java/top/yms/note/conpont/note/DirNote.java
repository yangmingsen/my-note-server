package top.yms.note.conpont.note;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteLuceneDataService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.req.NoteShareReqDto;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteShareInfo;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.service.NoteMetaService;
import top.yms.note.vo.NoteShareVo;

import javax.annotation.Resource;

/**
 * 此为目录笔记（特殊类型）
 */
@Component
public class DirNote implements Note, NoteLuceneDataService {

    @Resource
    private NoteMetaService noteMetaService;

    public int getSortValue() {
        return 99;
    }

    @Override
    public boolean support(String type) {
        if (StringUtils.isBlank(type)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean supportDestroy(String noteType) {
        return false;
    }

    @Override
    public void noteDestroy(Long id) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public boolean supportSave() {
        return false;
    }

    @Override
    public boolean supportEncrypt() {
        return true;
    }

    @Override
    public boolean noteDecrypt(Long id) {
        return false;
    }

    @Override
    public boolean noteEncrypt(Long id) {
        return false;
    }

    @Override
    public boolean supportExport(String noteType, String exportType) {
        return false;
    }

    @Override
    public String export(Long noteId, String exportType) {
        return null;
    }

    @Override
    public boolean supportVersion() {
        return false;
    }

    @Override
    public void addNoteVersion(INoteData iNoteData) {

    }

    @Override
    public boolean supportShare(String noteType) {
        return false;
    }

    @Override
    public NoteShareVo shareNoteGet(NoteShareReqDto noteId) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public void shareNoteClose(NoteShareReqDto noteId) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public NoteShareInfo shareNoteOpen(NoteShareReqDto noteId) {
        throw new BusinessException(BusinessErrorCode.E_204012);
    }

    @Override
    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteMeta noteMeta = noteMetaService.findOne(id);
        NoteLuceneIndex noteLuceneIndex = new NoteLuceneIndex();
        noteLuceneIndex.setId(id);
        noteLuceneIndex.setParentId(noteMeta.getParentId());
        noteLuceneIndex.setUserId(noteMeta.getUserId());
        noteLuceneIndex.setCreateDate(noteMeta.getCreateTime());
        noteLuceneIndex.setTitle(noteLuceneIndex.getTitle());
        noteLuceneIndex.setType(noteMeta.getType());
        noteLuceneIndex.setIsFile(noteMeta.getIsFile());
        noteLuceneIndex.setEncrypted(noteMeta.getEncrypted());
        //ret
        return noteLuceneIndex;
    }

    @Override
    public boolean supportGetLuceneData(String type) {
        return support(type);
    }
}
