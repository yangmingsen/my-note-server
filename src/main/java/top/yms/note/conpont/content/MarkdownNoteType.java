package top.yms.note.conpont.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class MarkdownNoteType extends AbstractNoteType {

    private final static Logger log = LoggerFactory.getLogger(MarkdownNoteType.class);

    private final static String supportType = "md";


    public int getSortValue() {
        return 1;
    }


    @Override
    public boolean support(String type) {
        return supportType.equals(type);
    }

    @Override
    public void save(Object data) throws BusinessException {
        NoteDataDto noteDataDto = (NoteDataDto) data;

        NoteData noteData = new NoteData();
        BeanUtils.copyProperties(noteDataDto, noteData);

        //更新笔记内容
        updateNoteData(noteData);

        //update noteIndex meta
        updateNoteIndex(null, noteData);

        //更新全局搜索索引
        NoteIndex oldNoteIdx = noteIndexMapper.selectByPrimaryKey(noteData.getId());
        saveSearchIndex(oldNoteIdx, noteDataDto.getContent());

        //版本记录
        saveDataVersion(noteData);
    }

    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteLuceneIndex noteLuceneIndex = packNoteIndexForNoteLuceneIndex(id);

        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        if (noteData == null) {
            log.warn("noteData目标不存在, 使用id={} 进行查询时", id);
        } else {
            noteLuceneIndex.setContent(noteData.getContent());
        }

        return noteLuceneIndex;
    }
}
