package top.yms.note.conpont.export;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.NoteDataExtendDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteExport;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.exception.NoteSystemException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteExportMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.NoteSystemErrorCode;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public abstract class AbstractNoteConvert implements NoteConvert{

    private final static Logger log = LoggerFactory.getLogger(AbstractNoteConvert.class);

    @Resource
    private NoteIndexMapper noteIndexMapper;

    @Resource
    private NoteDataMapper noteDataMapper;

    @Resource
    private IdWorker idWorker;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private NoteExportMapper noteExportMapper;

    @Resource
    private NoteFileMapper noteFileMapper;

    @Value("${note.export.tmp-path}")
    private String tmpExportPath;

    protected static final DataHolder HTML_TO_PDF_OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                    Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
                    , TocExtension.create()).toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
            .toImmutable();

    @Override
    public boolean support(String fromType, String toType) {
        return false;
    }

    protected String getTmpExportPath() {
        return tmpExportPath;
    }

    @Override
    public String convert(Long id) {
        //获取笔记基本数据
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        NoteDataExtendDto nte = new NoteDataExtendDto();
        nte.setNoteData(noteData);
        nte.setNoteIndex(noteIndex);
        nte.setUserId(LocalThreadUtils.getUserId());
        //前置检查
        if (!beforeConvert(nte)) {
            throw new BusinessException(BusinessErrorCode.E_204001);
        }
        //获取本地存储路径
        String localPath = geExportLocalPath();
        log.debug("get localPath={}", localPath);
        //处理转换
        doConvert(localPath, nte);
        //上传
        String fileId = uploadConvertFile(localPath, nte);
        //后置处理
        afterConvert(localPath, nte);
        return fileId;
    }

    /**
     * 获取当前本地临时存储文件路径
     * @return
     */
    protected String geExportLocalPath() {
        return getTmpExportPath()+idWorker.nextId()+"."+getExportType();
    }

    /**
     * 上传，记录等
     * @param localPath
     * @param iNoteData
     * @return
     */
    protected  String uploadConvertFile(String localPath, INoteData iNoteData) {
        String fileId;
        try {
            fileId = fileStoreService.saveFile(localPath);
        } catch (Exception e) {
            log.error("upload convert note error", e);
            throw new NoteSystemException(NoteSystemErrorCode.E_400003);
        }
        //save export meta info
        NoteExport exportMeta = new NoteExport();
        long eid = idWorker.nextId();
        exportMeta.setId(eid);
        exportMeta.setUserId(LocalThreadUtils.getUserId());
        exportMeta.setNoteId(iNoteData.getId());
        exportMeta.setExportType(getExportType());
        exportMeta.setLocalPath(localPath);
        exportMeta.setViewUrl(NoteConstants.getBaseUrl()+NoteConstants.getFileDownloadUrlSuffix(fileId));
        exportMeta.setCreateTime(new Date());
        noteExportMapper.insertSelective(exportMeta);
        //save note file info
        NoteFile noteFile = new NoteFile();
        noteFile.setFileId(fileId);
        noteFile.setName(eid+"."+getExportType());
        noteFile.setType(getExportType());
        noteFile.setSize((long)iNoteData.getContent().getBytes(StandardCharsets.UTF_8).length);
        noteFile.setUserId(iNoteData.getUserId());
        noteFile.setUrl(NoteConstants.getFileViewUrlSuffix(fileId));
        noteFile.setCreateTime(new Date());
        noteFile.setUpdateTime(new Date());
        noteFile.setNoteRef(iNoteData.getId());
        noteFileMapper.insertSelective(noteFile);
        return fileId;
    }

    /**
     * 获取当前导出类型
     * @return
     */
    protected abstract String getExportType();

    protected boolean beforeConvert(INoteData iNoteData) {return true;}

    /**
     * 执行转换
     * @param iNoteData
     * @throws BusinessException
     */
    abstract void doConvert(String localPath, INoteData iNoteData) throws BusinessException;

    protected void afterConvert(String localPath, INoteData iNoteData) {
        try {
            log.debug("prepare delete file => {}", localPath);
            Files.delete(Paths.get(localPath));
            log.debug("delete {} success", localPath);
        } catch (IOException e) {
            log.error("local file delete error", e);
            throw new NoteSystemException(NoteSystemErrorCode.E_400005);
        }
    }

    protected String getHtml(String body) {
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
        String htmlStr = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" +
                "" +  // add your stylesheets, scripts styles etc.
                // uncomment line below for adding style for custom embedded fonts
                nonLatinFonts +
                "</head><body>" + body + "\n" +
                "</body></html>";

        return htmlStr;
    }
}
