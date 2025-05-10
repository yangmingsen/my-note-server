package top.yms.note.conpont.export;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import top.yms.note.comm.BusinessErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.comm.NoteSystemErrorCode;
import top.yms.note.comm.NoteSystemException;
import top.yms.note.conpont.ComponentSort;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.NoteDataExtendDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteExport;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteExportMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
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

    @Override
    public int compareTo(@NotNull ComponentSort o) {
        return this.getSortValue()-o.getSortValue();
    }

    @Override
    public int getSortValue() {
        return 999;
    }

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
        String localPath = geExportLocalPath();
        //处理转换
        doConvert(localPath, nte);
        String fileId = uploadConvertFile(localPath, nte);
        //后置处理
        afterConvert(nte);
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
        exportMeta.setViewUrl(NoteConstants.getBaseUrl()+NoteConstants.getFileViewUrlSuffix(fileId));
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

    protected void afterConvert(INoteData iNoteData) {}
}
