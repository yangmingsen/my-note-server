package top.yms.note.conpont.export;

import com.vladsch.flexmark.docx.converter.DocxRenderer;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.BusinessErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.dto.INoteData;
import top.yms.note.exception.BusinessException;

import java.io.File;
import java.util.Arrays;

@Component
public class Markdown2DocxConvert extends AbstractNoteConvert{

    private final static Logger log = LoggerFactory.getLogger(Markdown2DocxConvert.class);

    // don't need to use pegdown options adapter. You can setup the options as you like. I find this is a quick way to add all the fixings
    final private static DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Arrays.asList(
                    DefinitionExtension.create(),
                    EmojiExtension.create(),
                    FootnoteExtension.create(),
                    StrikethroughSubscriptExtension.create(),
                    InsExtension.create(),
                    SuperscriptExtension.create(),
                    TablesExtension.create(),
                    TocExtension.create(),
                    SimTocExtension.create(),
                    WikiLinkExtension.create()
            ))
            .set(DocxRenderer.SUPPRESS_HTML, true)
            // the following two are needed to allow doc relative and site relative address resolution
//            .set(DocxRenderer.DOC_RELATIVE_URL, "file:///Users/vlad/src/pdf") // this will be used for URLs like 'images/...' or './' or '../'
//            .set(DocxRenderer.DOC_ROOT_URL, "file:///Users/vlad/src/pdf") // this will be used for URLs like: '/...'
            ;

    @Override
    public boolean support(String fromType, String toType) {
        return NoteConstants.MARKDOWN.equals(fromType) && getExportType().equals(toType);
    }

    @Override
    protected String getExportType() {
        return NoteConstants.DOCX;
    }

    @Override
    void doConvert(String localPath, INoteData iNoteData) throws BusinessException {
        String markdown = iNoteData.getContent();
        Parser PARSER = Parser.builder(OPTIONS).build();
        DocxRenderer RENDERER = DocxRenderer.builder(OPTIONS).build();
        Node document = PARSER.parse(markdown);
        // to get XML
        String xml = RENDERER.render(document);
        // or to control the package
        WordprocessingMLPackage template = DocxRenderer.getDefaultTemplate();
        RENDERER.render(document, template);
        File file = new File(localPath);
        try {
            template.save(file, Docx4J.FLAG_SAVE_ZIP_FILE);
        } catch (Exception ex) {
            log.error("markdown 2 docx error", ex);
            throw new BusinessException(BusinessErrorCode.E_204002);
        }
    }
}
