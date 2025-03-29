package top.yms.note.conpont.content;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
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


    public int getSortValue() {
        return 3;
    }

    @Override
    public boolean support(String type) {
        return NoteConstants.mindmapSuffix.equals(type);
    }

    final String noteMindMap = NoteConstants.noteMindMap;

    @Override
    public INoteData doGetContent(Long id) {
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
    public void doSave(INoteData iNoteData) throws BusinessException {
        NoteDataDto noteDataDto = (NoteDataDto) iNoteData;
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
                objId = saveRes.getObjectId(NoteConstants._id);
                upNoteIndex.setSiteId(objId.toString());
            } else {
                oldDoc = mongoTemplate.findById(noteIndex1.getSiteId(), Document.class, noteMindMap);
                ObjectId objectId = new ObjectId(noteIndex1.getSiteId());
                document.put(NoteConstants._id, objectId);
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
                mongoTemplate.remove(new Document(NoteConstants._id, objId), noteMindMap);
            }
            if (oldDoc != null) {
                mongoTemplate.save(oldDoc, noteMindMap);
            }
            throw new BusinessException(CommonErrorCode.E_203008);
        }
    }

    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteLuceneIndex noteLuceneIndex = packNoteIndexForNoteLuceneIndex(id);
        Object o= doGetContent(id);
        if (o == null) {
            return noteLuceneIndex;
        }
        NoteData noteData = (NoteData)o;
        JSONObject jsonObject = JSONObject.parseObject(noteData.getContent());
        StringBuilder contentStr = new StringBuilder();
        traverseJSONObject(jsonObject, contentStr);
        if (!StringUtils.isBlank(contentStr)) {
            noteLuceneIndex.setContent(contentStr.toString());
        }
        return noteLuceneIndex;
    }

    private void traverseJSONObject(Object dataObj, StringBuilder content) {
        if (dataObj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) dataObj;
            for(String key : jsonObject.keySet()) {
                Object objV = jsonObject.get(key);
                traverseJSONObject(objV, content);
            }
        } else if (dataObj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) dataObj;
            for (Object o2 : jsonArray) {
                traverseJSONObject(o2, content);
            }
        } else {
            String stringData = dataObj.toString();
            if (!stringData.contains("ObjectId")) {
                content.append(stringData).append(" ");
            }
        }
    }
}
