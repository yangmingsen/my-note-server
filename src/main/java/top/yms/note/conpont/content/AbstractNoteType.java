package top.yms.note.conpont.content;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.conpont.*;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.conpont.task.DelayExecuteAsyncTask;
import top.yms.note.dto.NoteIndexLuceneUpdateDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteDataVersionMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.service.NoteFileService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Created by yangmingsen on 2024/8/21.
 */
public abstract class AbstractNoteType implements NoteType, NoteLuceneDataService, NoteExport {

    private final static Logger log = LoggerFactory.getLogger(AbstractNoteType.class);

    @Autowired
    protected NoteDataMapper noteDataMapper;

    @Autowired
    protected NoteDataVersionMapper noteDataVersionMapper;

    @Autowired
    protected NoteIndexMapper noteIndexMapper;

    @Autowired
    protected NoteFileMapper noteFileMapper;

    @Autowired
    NoteFileService noteFileService;

    @Autowired
    protected FileStore fileStore;

    @Autowired
    @Qualifier(NoteConstants.noteLuceneSearch)
    protected NoteDataIndexService noteDataIndexService;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    private NoteAsyncExecuteTaskService noteAsyncExecuteTaskService;

    @Autowired
    private IdWorker idWorker;

    @Override
    public int compareTo(ComponentSort other) {
        return this.getSortValue()-other.getSortValue();
    }

    @Override
    public int getSortValue() {
        return 999;
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

    public Object getContent(Long id) {
        //修改访问时间
        NoteIndex upNoteIndex = new NoteIndex();
        upNoteIndex.setId(id);
        upNoteIndex.setViewTime(new Date());
        noteIndexMapper.updateByPrimaryKeySelective(upNoteIndex);

        return doGetContent(id);
    }

    protected Object doGetContent(Long id) {
        return noteDataMapper.selectByPrimaryKey(id);
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
     * @param noteData
     */
    protected void updateNoteData(NoteData noteData) {
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
     * @param noteData
     */
    protected void updateNoteIndex(NoteIndex noteIndex, NoteData noteData) {
        if (noteIndex == null) noteIndex = new NoteIndex();
        noteIndex.setId(noteData.getId());
        noteIndex.setUpdateTime(new Date());
        //更新大小
        long noteSize = noteData.getContent().getBytes(StandardCharsets.UTF_8).length;
        List<NoteFile> noteFiles = noteFileMapper.selectByNoteRef(noteData.getId());
        for(NoteFile noteFile : noteFiles) {
            noteSize+=noteFile.getSize();
        }
        noteIndex.setSize(noteSize);

        noteIndexMapper.updateByPrimaryKeySelective(noteIndex);

    }

    /**
     * 更新全局搜索索引
     * @param noteIndex
     * @param indexContent
     */
    protected void saveSearchIndex(NoteIndex noteIndex, String indexContent) {
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
     * @param noteData
     */
    protected void saveDataVersion(NoteData noteData) {
        //版本记录
        NoteDataVersion dataVersion = new NoteDataVersion();
        dataVersion.setNoteId(noteData.getId());
        dataVersion.setContent(noteData.getContent());
        dataVersion.setUserId(noteData.getUserId());
        dataVersion.setCreateTime(new Date());
        noteDataVersionMapper.insertSelective(dataVersion);
    }

    public abstract void save(Object data) throws BusinessException ;


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

        return noteLuceneIndex;
    }

    @Override
    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        throw new BusinessException(CommonErrorCode.E_200214);
    }
}
