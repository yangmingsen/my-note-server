package top.yms.note.conpont.content;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.INoteDataExt;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class WerNote extends AbstractNote {
    private final static Logger log = LoggerFactory.getLogger(WerNote.class);

    private final static String supportType = "wer";

    public int getSortValue() {
        return 2;
    }

    @Override
    public boolean support(String type) {
        return supportType.equals(type);
    }


    @Override
    public void doSave(INoteData iNoteData) throws BusinessException {
        INoteDataExt iNoteDataExt = (INoteDataExt) iNoteData;
        ObjectId objId = null;
        Document oldDoc = null;
        try {
            NoteData noteData = new NoteData();
            BeanUtils.copyProperties(iNoteDataExt, noteData);
            if (StringUtils.isBlank(iNoteDataExt.getTextContent())) {
                throw new BusinessException(NoteIndexErrorCode.E_203118);
            }
            //更新笔记内容
            updateNoteData(noteData);
            //更新mongo
            NoteIndex noteIndex = new NoteIndex();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NoteConstants.id, iNoteDataExt.getId());
            jsonObject.put(NoteConstants.textContent, iNoteDataExt.getTextContent());
            Document document = Document.parse(jsonObject.toString());
            NoteIndex oldNoteIdx = noteIndexMapper.selectByPrimaryKey(iNoteData.getId());
            if (StringUtils.isNotBlank(oldNoteIdx.getSiteId())) {
                oldDoc = mongoTemplate.findById(oldNoteIdx.getSiteId(), Document.class, NoteConstants.noteWerTextContent);
                ObjectId objectId = new ObjectId(oldNoteIdx.getSiteId());
                document.put(NoteConstants._id, objectId);
                mongoTemplate.save(document,  NoteConstants.noteWerTextContent);
            } else {
                Document saveRes = mongoTemplate.save(document, NoteConstants.noteWerTextContent);
                objId = saveRes.getObjectId(NoteConstants._id);
                noteIndex.setSiteId(objId.toString());
            }
            //update index
            updateNoteIndex(noteIndex, iNoteData);
            //更新全局搜索索引
            saveSearchIndex(oldNoteIdx, iNoteDataExt.getTextContent());
        } catch (Exception e) {
            log.error("save失败", e);
            if (objId != null) {
                mongoTemplate.remove(new Document(NoteConstants._id, objId), NoteConstants.noteWerTextContent);
            }
            if (oldDoc != null) {
                mongoTemplate.save(oldDoc, NoteConstants.noteWerTextContent);
            }
            throw new RuntimeException(e);
        }

    }

    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteLuceneIndex noteLuceneIndex = packNoteIndexForNoteLuceneIndex(id);
        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        if (noteData == null) {
            log.error("noteData目标不存在, 使用id={} 进行查询时", id);
            throw new BusinessException(NoteIndexErrorCode.E_203117);
        }
        String textContent = null;
        org.bson.Document mongoDoc = mongoTemplate
                .findOne(org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where(NoteConstants.id).is(id)),
                        org.bson.Document.class,
                        NoteConstants.noteWerTextContent);
        if (mongoDoc == null) {
            log.warn("根据id: {} 从mongo获取Wer数据为空", id);
        }  else {
            textContent = (String)mongoDoc.get(NoteConstants.textContent);
            //兼容之前错误数据
            if (StringUtils.isBlank(textContent)) {
                textContent = (String)mongoDoc.get("textConent");
            }
        }
        noteLuceneIndex.setContent(textContent);
        return noteLuceneIndex;
    }

    @Override
    public boolean supportVersion() {
        return true;
    }
}
