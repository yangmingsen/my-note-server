package top.yms.note.conpont.export;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.BusinessErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.dto.INoteData;
import top.yms.note.exception.BusinessException;

import java.io.FileOutputStream;
import java.io.OutputStream;

@Component
public class Markdown2PdfConvert extends AbstractNoteConvert {

    private final static Logger log = LoggerFactory.getLogger(Markdown2PdfConvert.class);

    static final DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                    Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
                    , TocExtension.create()).toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
            .toImmutable();

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
        Parser PARSER = Parser.builder(OPTIONS).build();
        HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();
        Node document = PARSER.parse(noteData.getContent());
        String htmlStr = RENDERER.render(document);
        // add embedded fonts for non-latin character set rendering
        // change file:///usr/local/fonts/ to your system's path for font installation Unix path or
        // on windows the path should start with `file:/X:/...` where `X:/...` is the drive
        // letter followed by the full installation path.
        //
        // Google Noto fonts can be downloaded from https://www.google.com/get/noto/
        // `arialuni.ttf` from https://www.wfonts.com/font/arial-unicode-ms
        String nonLatinFonts = "" +
                "<style>\n" +
                "@font-face {\n" +
                "  font-family: 'noto-cjk';\n" +
                "  src: url('file:/C:/Windows/Fonts/STKAITI.TTF');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "@font-face {\n" +
                "  font-family: 'noto-serif';\n" +
                "  src: url('file:/C:/Windows/Fonts/STKAITI.TTF');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "@font-face {\n" +
                "  font-family: 'noto-sans';\n" +
                "  src: url('file:/C:/Windows/Fonts/STKAITI.TTF');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "@font-face {\n" +
                "  font-family: 'noto-mono';\n" +
                "  src: url('file:/C:/Windows/Fonts/STKAITI.TTF');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "body {\n" +
                "    font-family: 'noto-sans', 'noto-cjk', sans-serif;\n" +
                "    overflow: hidden;\n" +
                "    word-wrap: break-word;\n" +
                "    font-size: 14px;\n" +
                "}\n" +
                "\n" +
                "var,\n" +
                "code,\n" +
                "kbd,\n" +
                "pre {\n" +
                "    font: 0.9em 'noto-mono', Consolas, \"Liberation Mono\", Menlo, Courier, monospace;\n" +
                "}\n" +
                "</style>\n" +
                "";
        htmlStr = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" +
                "" +  // add your stylesheets, scripts styles etc.
                // uncomment line below for adding style for custom embedded fonts
                nonLatinFonts +
                "</head><body>" + htmlStr + "\n" +
                "</body></html>";
        try(OutputStream os = new FileOutputStream(tmpPath)) {
            PdfConverterExtension.exportToPdf(os, htmlStr, "", PdfConverterExtension.DEFAULT_TEXT_DIRECTION.get(OPTIONS), PdfConverterExtension.PROTECTION_POLICY.get(OPTIONS));
        } catch (Exception e) {
            log.error("markdown to pdf error", e);
            throw new BusinessException(BusinessErrorCode.E_204002);
        }
    }
}
