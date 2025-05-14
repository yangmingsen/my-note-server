package top.yms.note.conpont.export;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.ast.Node;
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
public class Markdown2PdfConvert extends AbstractNoteConvert {

    private final static Logger log = LoggerFactory.getLogger(Markdown2PdfConvert.class);

    @Override
    public boolean support(String fromType, String toType) {
        return NoteConstants.MARKDOWN.equals(fromType) && getExportType().equals(toType);
    }

    @Override
    protected String getExportType() {
        return NoteConstants.PDF;
    }

    @Override
    public void doConvert(String tmpPath, INoteData iNoteData) throws BusinessException {
        //get note export data
        INoteData noteData = iNoteData.getNoteData();
        Parser PARSER = Parser.builder(HTML_TO_PDF_OPTIONS).build();
        HtmlRenderer RENDERER = HtmlRenderer.builder(HTML_TO_PDF_OPTIONS).build();
        Node document = PARSER.parse(noteData.getContent());
        String body = RENDERER.render(document);
        String htmlStr = getHtml(body);
        try(OutputStream os = new FileOutputStream(tmpPath)) {
            PdfConverterExtension.exportToPdf(os, htmlStr, "", PdfConverterExtension.DEFAULT_TEXT_DIRECTION.get(HTML_TO_PDF_OPTIONS), PdfConverterExtension.PROTECTION_POLICY.get(HTML_TO_PDF_OPTIONS));
        } catch (Exception e) {
            log.error("markdown to pdf error", e);
            throw new BusinessException(BusinessErrorCode.E_204002);
        }
    }
}
