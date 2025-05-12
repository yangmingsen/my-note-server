package top.yms.note.conpont.note;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import top.yms.note.comm.*;
import top.yms.note.conpont.*;
import top.yms.note.conpont.export.NoteFileExport;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.conpont.task.AsyncTask;
import top.yms.note.conpont.task.DelayExecuteAsyncTask;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.NoteDataExtendDto;
import top.yms.note.dto.NoteIndexLuceneUpdateDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.exception.NoteSystemException;
import top.yms.note.mapper.*;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.msgcd.NoteIndexErrorCode;
import top.yms.note.msgcd.NoteSystemErrorCode;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Created by yangmingsen on 2024/8/21.
 */
public abstract class AbstractNote implements Note, NoteLuceneDataService {

    private final static Logger log = LoggerFactory.getLogger(AbstractNote.class);

    @Value("${note.export.tmp-path}")
    protected String tmpExportPath;

    @Value("${note.encrypted.key}")
    private String encryptedKey;

    @Resource
    protected NoteDataMapper noteDataMapper;

    @Resource
    protected NoteDataVersionMapper noteDataVersionMapper;

    @Resource
    protected NoteIndexMapper noteIndexMapper;

    @Resource
    protected NoteFileMapper noteFileMapper;

    @Resource
    protected NoteFileService noteFileService;

    @Resource
    protected FileStoreService fileStoreService;

    @Resource
    protected MongoTemplate mongoTemplate;

    @Resource
    protected NoteAsyncExecuteTaskService noteAsyncExecuteTaskService;

    @Resource
    protected IdWorker idWorker;

    @Resource
    protected NoteFileExport noteFileExport;

    @Qualifier(NoteConstants.noteExpireTimeCache)
    @Resource
    protected NoteCacheService noteExpireCacheService;

    @Override
    public int compareTo(ComponentSort other) {
        return this.getSortValue()-other.getSortValue();
    }

    @Override
    public int getSortValue() {
        return 999;
    }

    protected String getEncryptedKey() {
        String key = Base64Util.decodeStr(encryptedKey);
        log.debug("getEncryptedKey={}", key);
        return key;
    }

    @Override
    public boolean supportEncrypt() {
        return false;
    }

    @Override
    public boolean supportGetEncryptDataForLucene() {
        return true;
    }

    @Override
    public boolean supportSave() {
        return false;
    }

    /**
     * 查找noteFile信息
     * @param id
     * @return
     */
    protected NoteFile findNoteFile(Long id) {
        List<NoteFile> noteFiles = noteFileMapper.selectByNoteRef(id);
        if (noteFiles.size() > 0) {
            return noteFiles.get(0);
        }
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        return noteFileService.findOne(noteIndex.getSiteId());
    }

    protected boolean beforeGetContent(INoteData iNoteData) {
        //token检查
        if (supportEncrypt() && NoteConstants.ENCRYPTED_FLAG.equals(iNoteData.getNoteIndex().getEncrypted())) {
            String tmpTokenKey =  NoteConstants.TMP_VISIT_TOKEN+iNoteData.getId();
            String tmpVisitToken = (String)noteExpireCacheService.find(tmpTokenKey);
            if (StringUtils.isBlank(tmpVisitToken)) {
                throw new BusinessException(BusinessErrorCode.E_204003);
            }
            String curTmpVisitToken = LocalThreadUtils.getTmpVisitToken();
            if (StringUtils.isBlank(curTmpVisitToken)) {
                throw new BusinessException(BusinessErrorCode.E_204005);
            }
            if (!tmpVisitToken.equals(curTmpVisitToken)) {
                throw new BusinessException(BusinessErrorCode.E_204004);
            }
        }
        return true;
    }

    protected void afterGetContent(INoteData iNoteData) {
        //修改访问时间
        NoteIndex upNoteIndex = new NoteIndex();
        upNoteIndex.setId(iNoteData.getId());
        upNoteIndex.setViewTime(new Date());
        noteIndexMapper.updateByPrimaryKeySelective(upNoteIndex);
        //解密处理
        if (supportEncrypt() && NoteConstants.ENCRYPTED_FLAG.equals(iNoteData.getNoteIndex().getEncrypted())) {
            String content = decryptContent(iNoteData.getContent());
            iNoteData.setContent(content);
            //clear tmpTokenKey
            String tmpTokenKey =  NoteConstants.TMP_VISIT_TOKEN+iNoteData.getId();
            noteExpireCacheService.delete(tmpTokenKey);
        }
    }

    public INoteData getContent(Long id) {
        NoteIndex noteMeta = noteIndexMapper.selectByPrimaryKey(id);
        NoteDataExtendDto nde  = new NoteDataExtendDto();
        nde.setNoteIndex(noteMeta);
        nde.setUserId(LocalThreadUtils.getUserId());
        //前置处理
        if (!beforeGetContent(nde)) {
            return null;
        }
        //获取数据
        NoteData iNoteData = (NoteData)doGetContent(id);
        nde.setNoteData(iNoteData);
        //后置处理
        afterGetContent(nde);
        //返回数据
        return nde.getNoteData();
    }

    protected INoteData doGetContent(Long id) {
        INoteData iNoteData = noteDataMapper.selectByPrimaryKey(id);
        return iNoteData;
    }

    private static final String [] ILLEGAL_LIST = {
            "<p><br></p>",
            "<p style=\"text-align: start;\"><br></p>"
    };

    protected boolean checkContent(String content) {
        if (content == null || StringUtils.isBlank(content)) {
            return true;
        }
        for(String illegalStr : ILLEGAL_LIST) {
            if (content.equals(illegalStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新笔记内容
     * @param iNoteData
     */
    protected void updateNoteData(INoteData iNoteData) {
        NoteData noteData = (NoteData)iNoteData;
        Long id = noteData.getId();
        NoteData dbNote = noteDataMapper.findById(id);
        if (checkContent(noteData.getContent())) {
            throw new BusinessException(NoteIndexErrorCode.E_203112);
        }
        Date opTime = new Date();
        if (dbNote == null) {
            noteData.setCreateTime(opTime);
            noteDataMapper.insert(noteData);
        } else {
            noteData.setUpdateTime(opTime);
            noteDataMapper.updateByPrimaryKeySelective(noteData);
        }
    }

    /**
     * 更新笔记元数据
     * @param noteIndex
     * @param iNoteData
     */
    protected void updateNoteMetaInfo(NoteIndex noteIndex, INoteData iNoteData) {
        if (noteIndex == null) noteIndex = new NoteIndex();
        noteIndex.setId(iNoteData.getId());
        noteIndex.setUpdateTime(new Date());
        //更新大小
        long noteSize = iNoteData.getContent().getBytes(StandardCharsets.UTF_8).length;
        List<NoteFile> noteFiles = noteFileMapper.selectByNoteRef(iNoteData.getId());
        for(NoteFile noteFile : noteFiles) {
            noteSize+=noteFile.getSize();
        }
        noteIndex.setSize(noteSize);
        noteIndexMapper.updateByPrimaryKeySelective(noteIndex);
    }

    /**
     * 更新全局搜索索引
     * @param noteIndex
     */
    protected void updateNoteMetaToLuceneSearch(NoteIndex noteIndex) {
        DelayExecuteAsyncTask indexUpdateDelayTask = DelayExecuteAsyncTask.Builder
                .build()
                .type(AsyncTaskEnum.SYNC_Note_Index_UPDATE)
                .executeType(AsyncExcuteTypeEnum.DELAY_EXC_TASK)
                .taskId(idWorker.nextId())
                .taskName(AsyncTaskEnum.SYNC_Note_Index_UPDATE.getName())
                .createTime(new Date())
                .userId(LocalThreadUtils.getUserId())
                .taskInfo(NoteIndexLuceneUpdateDto.Builder.build().type(NoteIndexLuceneUpdateDto.updateNoteContent).data(noteIndex.getId()).get())
                .get();
        noteAsyncExecuteTaskService.addTask(indexUpdateDelayTask);
    }

    /**
     * add版本记录
     */
    protected void saveDataVersion(INoteData inoteData) {
        //版本记录
        NoteDataVersion dataVersion = new NoteDataVersion();
        dataVersion.setNoteId(inoteData.getId());
        dataVersion.setContent(inoteData.getContent());
        dataVersion.setUserId(inoteData.getUserId());
        dataVersion.setCreateTime(new Date());
        noteDataVersionMapper.insertSelective(dataVersion);
    }

    protected boolean beforeSave(INoteData iNoteData) {
        if (!supportSave()) {
            log.info("当前组件不支持该类型数据保存");
            return false;
        }
        //加密处理
        if (supportEncrypt()) {
            Long id = iNoteData.getId();
            NoteIndex noteMeta = noteIndexMapper.selectByPrimaryKey(id);
            if (NoteConstants.ENCRYPTED_FLAG.equals(noteMeta.getEncrypted())) {
                //设置加密内容
                String content = iNoteData.getContent();
                //加密
                String encryptContent = encryptContent(content);
                iNoteData.setContent(encryptContent);
            }
        }
        return true;
    }

    public  void save(INoteData iNoteData) throws BusinessException  {
        //执行前检查
        if (!beforeSave(iNoteData)) {  return;  }
        //执行保存
        doSave(iNoteData);
        //保存后处理
        afterSave(iNoteData);
    }

    protected void afterSave(INoteData iNoteData) {
        //更新笔记元数据
        NoteIndex noteMeta = noteIndexMapper.selectByPrimaryKey(iNoteData.getId());
        updateNoteMetaInfo(noteMeta, iNoteData);
        //更新全局搜索
        updateNoteMetaToLuceneSearch(noteMeta);
        //版本支持
        if (supportVersion()) {
            //添加笔记版本
            addNoteVersion(iNoteData);
            //笔记版本优化
            AsyncTask asyncTask = AsyncTask.Builder.build()
                    .taskId(IdWorkerUtils.getId())
                    .executeType(AsyncExcuteTypeEnum.SYNC_TASK)
                    .type(AsyncTaskEnum.NOTE_CONTENT_VERSION_OPTIMIZE)
                    .taskName(AsyncTaskEnum.NOTE_CONTENT_VERSION_OPTIMIZE.getName())
                    .createTime(new Date())
                    .userId(LocalThreadUtils.getUserId())
                    .taskInfo(iNoteData.getId())
                    .get();
            //加密笔记禁止进入版本优化
            if (NoteConstants.ENCRYPTED_UN_FLAG.equals(noteMeta.getEncrypted())) {
                noteAsyncExecuteTaskService.addTask(asyncTask);
            }
        }
    }

    abstract void doSave(INoteData iNoteData) throws BusinessException ;

    protected NoteLuceneIndex packNoteIndexForNoteLuceneIndex(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        if (noteIndex == null) {
            log.error("noteIndex目标不存在, 使用id={} 进行查询时", id);
            throw new BusinessException(NoteIndexErrorCode.E_203117);
        }
        NoteLuceneIndex noteLuceneIndex = new NoteLuceneIndex();
        BeanUtils.copyProperties(noteIndex, noteLuceneIndex);
        //NoteIndex中的创建时间是 createTime， 而NoteLuceneIndex中是createDate。要注意
        noteLuceneIndex.setCreateDate(noteIndex.getCreateTime());
        //bug 2025-03-22 没有设置title
        noteLuceneIndex.setTitle(noteIndex.getName());
        return noteLuceneIndex;
    }

    @Override
    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        throw new BusinessException(CommonErrorCode.E_200214);
    }

    public boolean supportGetLuceneData(String type) {
        return support(type);
    }

    @Override
    public boolean supportVersion() {
        return false;
    }

    @Override
    public void addNoteVersion(INoteData iNoteData) {
        saveDataVersion(iNoteData);
    }

    @Override
    public boolean supportExport(String noteType, String exportType) {
        throw new BusinessException(CommonErrorCode.E_200214);
    }

    @Override
    public String export(Long noteId, String exportType) {
        throw new BusinessException(CommonErrorCode.E_200214);
    }

    /**
     * 笔记内容解密
     * @param content content
     * @return
     */
    protected String decryptContent(String content) {
        try {
            content = AESCipher.decrypt(content, getEncryptedKey());
            log.debug("解密后:\n{}", content);
        } catch (Exception ex) {
            log.error("decrypt error", ex);
            throw new NoteSystemException(NoteSystemErrorCode.E_400002);
        }
        return content;
    }
    /**
     * 笔记内容加密
     * @param content content
     * @return
     */
    protected String encryptContent(String content) {
        try {
            content = AESCipher.encrypt(content, getEncryptedKey());
            log.debug("加密后:\n{}", content);
        } catch (Exception ex) {
            log.error("encrypt error", ex);
            throw new NoteSystemException(NoteSystemErrorCode.E_400001);
        }
        return content;
    }

    @Override
    public boolean noteDecrypt(Long id) {
        if (!supportEncrypt()) {
            log.info("当前组件不支持解密");
            return false;
        }
        return true;
    }

    @Override
    public boolean noteEncrypt(Long id) {
        if (!supportEncrypt()) {
            log.info("当前组件不支持加密");
            return false;
        }
        return true;
    }
}
