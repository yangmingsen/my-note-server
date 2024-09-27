package top.yms.note.conpont.content;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class MarkdownNoteType extends AbstractNoteType {
    private final static String supportType = "md";

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
        saveSearchIndex(oldNoteIdx, noteDataDto.getTextContent());

        //版本记录
        saveDataVersion(noteData);
    }
}
