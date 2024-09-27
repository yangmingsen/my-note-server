package top.yms.note.conpont.content;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.comm.Constants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class WerNoteType extends AbstractNoteType {
    private final static Logger log = LoggerFactory.getLogger(WerNoteType.class);

    private final static String supportType = "wer";

    @Override
    public boolean support(String type) {
        return supportType.equals(type);
    }


    @Override
    public void save(Object data) throws BusinessException {
        NoteDataDto noteDataDto = (NoteDataDto) data;
        ObjectId objId = null;
        Document oldDoc = null;
        try {
            NoteData noteData = new NoteData();
            BeanUtils.copyProperties(noteDataDto, noteData);
            if (StringUtils.isBlank(noteDataDto.getTextContent())) {
                throw new BusinessException(NoteIndexErrorCode.E_203118);
            }

            //更新笔记内容
            updateNoteData(noteData);

            //更新mongo
            NoteIndex noteIndex = new NoteIndex();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", noteDataDto.getId());
            jsonObject.put("textConent", noteDataDto.getTextContent());
            Document document = Document.parse(jsonObject.toString());
            NoteIndex oldNoteIdx = noteIndexMapper.selectByPrimaryKey(noteData.getId());
            if (StringUtils.isNotBlank(oldNoteIdx.getSiteId())) {
                oldDoc = mongoTemplate.findById(oldNoteIdx.getSiteId(), Document.class, Constants.noteWerTextContent);
                ObjectId objectId = new ObjectId(oldNoteIdx.getSiteId());
                document.put("_id", objectId);
                mongoTemplate.save(document,  Constants.noteWerTextContent);
            } else {
                Document saveRes = mongoTemplate.save(document, Constants.noteWerTextContent);
                objId = saveRes.getObjectId("_id");
                noteIndex.setSiteId(objId.toString());
            }

            //update index
            updateNoteIndex(noteIndex, noteData);

            //更新全局搜索索引
            saveSearchIndex(oldNoteIdx, noteDataDto.getTextContent());

            //版本记录
            saveDataVersion(noteData);
        } catch (Exception e) {
            log.error("save失败", e);
            if (objId != null) {
                mongoTemplate.remove(new Document("_id", objId), Constants.noteWerTextContent);
            }
            if (oldDoc != null) {
                mongoTemplate.save(oldDoc, Constants.noteWerTextContent);
            }
            throw new RuntimeException(e);
        }

    }
}
