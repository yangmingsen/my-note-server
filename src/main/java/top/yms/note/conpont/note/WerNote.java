package top.yms.note.conpont.note;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.msgcd.NoteIndexErrorCode;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.INoteDataExt;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.utils.AESCipher;

import java.util.List;

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
    public boolean supportEncrypt() {
        return true;
    }

    @Override
    public boolean support(String type) {
        return supportType.equals(type);
    }

    @Override
    public boolean supportSave() {
        return true;
    }

    public boolean noteEncrypt(Long id) {
        if (!super.noteEncrypt(id)) {
            return false;
        }
        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        //加密 content
        noteData.setContent(encryptContent(noteData.getContent()));
        //更新
        noteDataMapper.updateByPrimaryKeySelective(noteData);
        //更新mongo
        org.bson.Document mongoDoc = mongoTemplate
                .findOne(org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where(NoteConstants.id).is(id)),
                        org.bson.Document.class,
                        NoteConstants.noteWerTextContent);
        if (mongoDoc == null) {
            throw new BusinessException(CommonErrorCode.E_200206);
        }
        String textContent = (String)mongoDoc.get(NoteConstants.textContent);
        //兼容之前错误数据
        if (StringUtils.isBlank(textContent)) {
            textContent = (String)mongoDoc.get("textConent");
        }
        //加密mongo textContent
        textContent = encryptContent(textContent);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NoteConstants.id, id);
        jsonObject.put(NoteConstants.textContent, textContent);
        Document document = Document.parse(jsonObject.toString());
        //更新mongo内容
        //get note 旧元数据
        NoteIndex noteMeta = noteIndexMapper.selectByPrimaryKey(id);
        mongoTemplate.findById(noteMeta.getSiteId(), Document.class, NoteConstants.noteWerTextContent);
        ObjectId objectId = new ObjectId(noteMeta.getSiteId());
        document.put(NoteConstants._id, objectId);
        mongoTemplate.save(document,  NoteConstants.noteWerTextContent);
        //加密data version
        List<NoteDataVersion> noteDataVersions = noteDataVersionMapper.selectByNoteId(id);
        for(NoteDataVersion dataVersion : noteDataVersions) {
            dataVersion.setContent(encryptContent(dataVersion.getContent()));
            noteDataVersionMapper.updateByPrimaryKeySelective(dataVersion);
        }
        return true;
    }

    public boolean noteDecrypt(Long id) {
        if (!super.noteDecrypt(id)) {
            return false;
        }
        NoteData noteData = noteDataMapper.selectByPrimaryKey(id);
        //解密
        noteData.setContent(decryptContent(noteData.getContent()));
        //更新
        noteDataMapper.updateByPrimaryKeySelective(noteData);
        //更新mongo
        org.bson.Document mongoDoc = mongoTemplate
                .findOne(org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where(NoteConstants.id).is(id)),
                        org.bson.Document.class,
                        NoteConstants.noteWerTextContent);
        if (mongoDoc == null) {
            throw new BusinessException(CommonErrorCode.E_200206);
        }
        String textContent = (String)mongoDoc.get(NoteConstants.textContent);
        //兼容之前错误数据
        if (StringUtils.isBlank(textContent)) {
            textContent = (String)mongoDoc.get("textConent");
        }
        textContent = decryptContent(textContent);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NoteConstants.id, id);
        jsonObject.put(NoteConstants.textContent, textContent);
        Document document = Document.parse(jsonObject.toString());
        //更新mongo内容
        //get note 旧元数据
        NoteIndex noteMeta = noteIndexMapper.selectByPrimaryKey(id);
        mongoTemplate.findById(noteMeta.getSiteId(), Document.class, NoteConstants.noteWerTextContent);
        ObjectId objectId = new ObjectId(noteMeta.getSiteId());
        document.put(NoteConstants._id, objectId);
        mongoTemplate.save(document,  NoteConstants.noteWerTextContent);
        //解密 data version
        List<NoteDataVersion> noteDataVersions = noteDataVersionMapper.selectByNoteId(id);
        for(NoteDataVersion dataVersion : noteDataVersions) {
            dataVersion.setContent(decryptContent(dataVersion.getContent()));
            noteDataVersionMapper.updateByPrimaryKeySelective(dataVersion);
        }
        return true;
    }

    public void updateNoteData(INoteData iNoteData) {
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
            super.updateNoteData(noteData);
            //get note 旧元数据
            NoteIndex oldNoteMeta = noteIndexMapper.selectByPrimaryKey(iNoteData.getId());
            //更新mongo
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NoteConstants.id, iNoteDataExt.getId());
            String textContent = iNoteDataExt.getTextContent();
            if (supportEncrypt()) {
                //加密处理
                if (NoteConstants.ENCRYPTED_FLAG.equals(oldNoteMeta.getEncrypted())) {
                    textContent = AESCipher.encrypt(iNoteDataExt.getTextContent(), getEncryptedKey());
                }
            }
            jsonObject.put(NoteConstants.textContent, textContent);
            Document document = Document.parse(jsonObject.toString());
            if (StringUtils.isNotBlank(oldNoteMeta.getSiteId())) {
                //更新mongo内容
                oldDoc = mongoTemplate.findById(oldNoteMeta.getSiteId(), Document.class, NoteConstants.noteWerTextContent);
                ObjectId objectId = new ObjectId(oldNoteMeta.getSiteId());
                document.put(NoteConstants._id, objectId);
                mongoTemplate.save(document,  NoteConstants.noteWerTextContent);
            } else {
                Document saveRes = mongoTemplate.save(document, NoteConstants.noteWerTextContent);
                objId = saveRes.getObjectId(NoteConstants._id);
                NoteIndex noteMeta = new NoteIndex();
                noteMeta.setSiteId(objId.toString());
                noteMeta.setId(iNoteData.getId());
                noteIndexMapper.updateByPrimaryKeySelective(noteMeta);
            }
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

    public void doSave(INoteData iNoteData) throws BusinessException {
        updateNoteData(iNoteData);
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
        if (NoteConstants.ENCRYPTED_FLAG.equals(noteLuceneIndex.getEncrypted())) {
            if (supportGetEncryptDataForLucene()) {
                textContent = decryptContent(textContent);
            } else {
                throw new BusinessException(BusinessErrorCode.E_204000);
            }
        }
        noteLuceneIndex.setContent(textContent);
        return noteLuceneIndex;
    }

    @Override
    public boolean supportVersion() {
        return true;
    }

    @Override
    public boolean supportExport(String noteType, String exportType) {
        boolean supportExport = StringUtils.equalsAny(exportType, NoteConstants.PDF);
        return support(noteType) && supportExport;
    }

    @Override
    public String export(Long noteId, String exportType) {
        return noteFileExport.noteExport(noteId, NoteConstants.WER, exportType);
    }
}
