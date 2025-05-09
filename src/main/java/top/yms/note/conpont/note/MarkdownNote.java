package top.yms.note.conpont.note;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.comm.BusinessErrorCode;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.entity.*;
import top.yms.note.entity.NoteExport;
import top.yms.note.exception.BusinessException;
import top.yms.note.utils.LocalThreadUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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

    public void updateNoteMetaInfo(NoteIndex noteIndex,
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
        if (exportType.equals(NoteConstants.PDF)) {
            String tmpPath = tmpExportPath+idWorker.nextId()+"."+NoteConstants.PDF;
            //get note export data
            INoteData noteData = noteDataMapper.selectByPrimaryKey(noteId);
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
            String fileId = null;
            try {
                OutputStream os = new FileOutputStream(tmpPath);
                PdfConverterExtension.exportToPdf(os, htmlStr, "", PdfConverterExtension.DEFAULT_TEXT_DIRECTION.get(OPTIONS), PdfConverterExtension.PROTECTION_POLICY.get(OPTIONS));
                fileId = fileStoreService.saveFile(tmpPath);
            } catch (Exception e) {
//                log.error("igo err", e);
            }
            //save export meta info
            NoteExport exportMeta = new NoteExport();
            long eid = idWorker.nextId();
            exportMeta.setId(eid);
            exportMeta.setUserId(LocalThreadUtils.getUserId());
            exportMeta.setNoteId(noteId);
            exportMeta.setExportType(exportType);
            exportMeta.setLocalPath(tmpPath);
            exportMeta.setViewUrl(NoteConstants.getBaseUrl()+NoteConstants.getFileViewUrlSuffix(fileId));
            exportMeta.setCreateTime(new Date());
            noteExportMapper.insertSelective(exportMeta);
            //save note file info
            NoteFile noteFile = new NoteFile();
            noteFile.setFileId(fileId);
            noteFile.setName(eid+"."+NoteConstants.PDF);
            noteFile.setType(NoteConstants.PDF);
            noteFile.setSize((long)htmlStr.getBytes(StandardCharsets.UTF_8).length);
            noteFile.setUserId(LocalThreadUtils.getUserId());
            noteFile.setUrl(NoteConstants.getFileViewUrlSuffix(fileId));
            noteFile.setCreateTime(new Date());
            noteFile.setUpdateTime(new Date());
            noteFile.setNoteRef(noteId);
            noteFileMapper.insertSelective(noteFile);
            return fileId;
        } else {
            throw new BusinessException(CommonErrorCode.E_100101);
        }

    }

    static final DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                    Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
                    , TocExtension.create()).toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
            .toImmutable();

}
