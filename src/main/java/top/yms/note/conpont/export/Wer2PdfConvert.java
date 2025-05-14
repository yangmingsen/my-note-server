package top.yms.note.conpont.export;

import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.dto.INoteData;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.BusinessErrorCode;

import java.io.FileOutputStream;
import java.io.OutputStream;

@Component
public class Wer2PdfConvert extends AbstractNoteConvert {

    private final static Logger log = LoggerFactory.getLogger(Wer2PdfConvert.class);

    @Override
    public boolean support(String fromType, String toType) {
        return NoteConstants.WER.equals(fromType) && getExportType().equals(toType);
    }

    @Override
    protected String getExportType() {
        return NoteConstants.PDF;
    }

    @Override
    void doConvert(String localPath, INoteData iNoteData) throws BusinessException {
        String body = iNoteData.getContent();
        String htmlStr = getHtml(body);
        try(OutputStream os = new FileOutputStream(localPath)) {
            PdfConverterExtension.exportToPdf(os, htmlStr, "", PdfConverterExtension.DEFAULT_TEXT_DIRECTION.get(HTML_TO_PDF_OPTIONS), PdfConverterExtension.PROTECTION_POLICY.get(HTML_TO_PDF_OPTIONS));
        } catch (Exception e) {
            log.error("markdown to pdf error", e);
            throw new BusinessException(BusinessErrorCode.E_204002);
        }
    }
}
