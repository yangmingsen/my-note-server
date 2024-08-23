package top.yms.note.conpont.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import top.yms.note.comm.Constants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.conpont.FileStore;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.dto.NoteLuceneIndex;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteDataVersionMapper;
import top.yms.note.mapper.NoteIndexMapper;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Created by yangmingsen on 2024/8/21.
 */
public abstract class AbstractNoteType implements NoteType {

    @Autowired
    protected NoteDataMapper noteDataMapper;

    @Autowired
    protected NoteDataVersionMapper noteDataVersionMapper;

    @Autowired
    protected NoteIndexMapper noteIndexMapper;

    @Autowired
    protected FileStore fileStore;

    @Autowired
    @Qualifier(Constants.noteLuceneSearch)
    protected NoteDataIndexService noteDataIndexService;


    public Object getContent(Long id) {
        return noteDataMapper.selectByPrimaryKey(id);
    }


    private static final String [] ILLEGAL_LIST = {
            "<p><br></p>",
            "<p style=\"text-align: start;\"><br></p>"
    };
    private boolean checkContent(String content) {
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
    public void save(Object data) throws BusinessException {
        NoteData noteData = (NoteData) data;

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

        //更新index信息
        NoteIndex noteIndex = new NoteIndex();
        noteIndex.setId(id);
        noteIndex.setUpdateTime(opTime);
        noteIndex.setSize((long)noteData.getContent().getBytes(StandardCharsets.UTF_8).length);
        noteIndexMapper.updateByPrimaryKeySelective(noteIndex);

        //通知更新lucene索引
        NoteIndex noteIndex1 = noteIndexMapper.selectByPrimaryKey(id);
        NoteLuceneIndex noteLuceneIndex = new NoteLuceneIndex();
        noteLuceneIndex.setId(id);
        noteLuceneIndex.setUserId(noteIndex1.getUserId());
        noteLuceneIndex.setParentId(noteIndex1.getParentId());
        noteLuceneIndex.setTitle(noteIndex1.getName());
        noteLuceneIndex.setContent(noteData.getContent());
        noteLuceneIndex.setIsFile(noteIndex1.getIsile());
        noteLuceneIndex.setType(noteIndex1.getType());
        noteLuceneIndex.setCreateDate(opTime);
        noteDataIndexService.update(noteLuceneIndex);

        //版本记录
        NoteDataVersion dataVersion = new NoteDataVersion();
        dataVersion.setNoteId(id);
        dataVersion.setContent(noteData.getContent());
        dataVersion.setUserId(noteData.getUserId());
        dataVersion.setCreateTime(opTime);
        noteDataVersionMapper.insertSelective(dataVersion);
    }

}
