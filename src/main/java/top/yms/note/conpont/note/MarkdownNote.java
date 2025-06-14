package top.yms.note.conpont.note;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteMeta;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;

import java.util.List;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class MarkdownNote extends AbstractNote {

    private final static Logger log = LoggerFactory.getLogger(MarkdownNote.class);

    private final static String supportType = NoteConstants.markdownSuffix;

    public int getSortValue() {
        return 1;
    }

    @Override
    public boolean support(String type) {
        return supportType.equals(type);
    }

    @Override
    public boolean supportEncrypt() {
        return true;
    }

    public boolean noteDecrypt(Long id) {
        if (!super.noteDecrypt(id)) {
            return false;
        }
        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        //解密
        noteData.setContent(decryptContent(noteData.getContent()));
        noteDataMapper.updateByPrimaryKeySelective(noteData);
        //解密data version
        List<NoteDataVersion> noteDataVersions = noteDataVersionMapper.selectByNoteId(id);
        for(NoteDataVersion dataVersion : noteDataVersions) {
            dataVersion.setContent(decryptContent(dataVersion.getContent()));
            noteDataVersionMapper.updateByPrimaryKeySelective(dataVersion);
        }
        return true;
    }

    public boolean noteEncrypt(Long id) {
        if (!super.noteEncrypt(id)) {
            return false;
        }
        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        //加密
        noteData.setContent(encryptContent(noteData.getContent()));
        noteDataMapper.updateByPrimaryKeySelective(noteData);
        //加密data version
        List<NoteDataVersion> noteDataVersions = noteDataVersionMapper.selectByNoteId(id);
        for(NoteDataVersion dataVersion : noteDataVersions) {
            dataVersion.setContent(encryptContent(dataVersion.getContent()));
            noteDataVersionMapper.updateByPrimaryKeySelective(dataVersion);
        }
        return true;
    }

    @Override
    public boolean supportSave() {
        return true;
    }

    public void updateNoteData(INoteData iNoteData) {
        NoteData noteData = new NoteData();
        BeanUtils.copyProperties(iNoteData, noteData);
        super.updateNoteData(noteData);
    }

    public void updateNoteMetaInfo(NoteMeta noteMeta,
                                   INoteData iNoteData) {
        //update noteIndex meta
        super.updateNoteMetaInfo(null, iNoteData);
    }


    public void doSave(INoteData iNoteData) throws BusinessException {
        updateNoteData(iNoteData);
    }

    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteLuceneIndex noteLuceneIndex = packNoteIndexForNoteLuceneIndex(id);
        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        if (noteData == null) {
            log.error("noteData目标不存在, 使用id={} 进行查询时", id);
            throw new BusinessException(CommonErrorCode.E_200206);
        }
        String content = noteData.getContent();
        if (NoteConstants.ENCRYPTED_FLAG.equals(noteLuceneIndex.getEncrypted())) {
            if (supportGetEncryptDataForLucene()) {
                content = decryptContent(content);
            } else {
                throw new BusinessException(BusinessErrorCode.E_204000);
            }
        }
        noteLuceneIndex.setContent(content);
        return noteLuceneIndex;
    }

    @Override
    public boolean supportVersion() {
        return true;
    }

    @Override
    public boolean supportExport(String noteType, String exportType) {
        boolean supportExport = StringUtils.equalsAny(exportType, NoteConstants.PDF, NoteConstants.DOCX);
        return support(noteType) && supportExport;
    }

    @Override
    public String export(Long noteId, String exportType) {
        return noteFileExport.noteExport(noteId, NoteConstants.MARKDOWN, exportType);
    }

    static final DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                    Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
                    , TocExtension.create()).toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
            .toImmutable();

    public boolean supportShare(String noteType) {
        return support(noteType);
    }

}
