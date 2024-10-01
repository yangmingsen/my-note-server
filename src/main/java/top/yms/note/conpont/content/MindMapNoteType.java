package top.yms.note.conpont.content;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.utils.LocalThreadUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Created by yangmingsen on 2024/9/24.
 */
@Component
public class MindMapNoteType extends AbstractNoteType{

    private final static Logger log = LoggerFactory.getLogger(MindMapNoteType.class);

    @Override
    public boolean support(String type) {
        return Constants.mindmapSuffix.equals(type);
    }

    final String noteMindMap = Constants.noteMindMap;

    @Override
    public Object doGetContent(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);

        NoteData res = new NoteData();
        Document resDoc = mongoTemplate.findById(noteIndex.getSiteId(), Document.class, noteMindMap);
        if (resDoc == null) {
            return null;
        }
        res.setUserId(LocalThreadUtils.getUserId());
        res.setId(id);
        res.setContent(resDoc.toJson());

        return res;
    }

    @Override
    public void save(Object data) throws BusinessException {
        NoteDataDto noteDataDto = (NoteDataDto) data;
        String jsonContent = noteDataDto.getContent();
        Long noteId = noteDataDto.getId();
        ObjectId objId = null;
        NoteIndex upNoteIndex = new NoteIndex();
        Document oldDoc = null;
        try {
            Document document = Document.parse(jsonContent);
            NoteIndex noteIndex1 = noteIndexMapper.selectByPrimaryKey(noteId);
            if (StringUtils.isBlank(noteIndex1.getSiteId())) {
                Document saveRes = mongoTemplate.save(document, noteMindMap);
                objId = saveRes.getObjectId("_id");
                upNoteIndex.setSiteId(objId.toString());
            } else {
                oldDoc = mongoTemplate.findById(noteIndex1.getSiteId(), Document.class, noteMindMap);
                ObjectId objectId = new ObjectId(noteIndex1.getSiteId());
                document.put("_id", objectId);
                mongoTemplate.save(document, noteMindMap);
            }

            Date opTime = new Date();
            long size = jsonContent.getBytes(StandardCharsets.UTF_8).length;
            //更新index信息
            upNoteIndex.setId(noteId);
            upNoteIndex.setUpdateTime(opTime);
            upNoteIndex.setSize(size);
            noteIndexMapper.updateByPrimaryKeySelective(upNoteIndex);

        } catch (Exception e) {
            log.error("MindMapNoteType#save异常", e);
            //回滚mongo数据
            if (objId != null) {
                mongoTemplate.remove(new Document("_id", objId), noteMindMap);
            }
            if (oldDoc != null) {
                mongoTemplate.save(oldDoc, noteMindMap);
            }
            throw new BusinessException(CommonErrorCode.E_203008);
        }
    }
}
