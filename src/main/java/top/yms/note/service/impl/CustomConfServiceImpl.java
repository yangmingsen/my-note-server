package top.yms.note.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.service.NoteCustomConfService;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by yangmingsen on 2024/9/27.
 */
@Service
public class CustomConfServiceImpl implements NoteCustomConfService {

    private static final Logger log = LoggerFactory.getLogger(CustomConfServiceImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private NoteRedisCacheService cacheService;

    public void updateUserConfig(JSONObject jsonObject) {
        Long userId = LocalThreadUtils.getUserId();
        Document oldDoc = mongoTemplate.findOne(Query.query(Criteria.where(NoteConstants.userid).is(userId)), Document.class, NoteConstants.customConfig);
        if (oldDoc == null) {
            jsonObject.put(NoteConstants.userid, userId);
            Document newDoc = Document.parse(jsonObject.toString());
            Document saveRes = mongoTemplate.save(newDoc, NoteConstants.customConfig);
            //决定是否保存
            ObjectId objId = saveRes.getObjectId("_id");
            log.debug("updateUserConfig_创建成功");
        } else {
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                oldDoc.put(key, value);
            }
            mongoTemplate.save(oldDoc,  NoteConstants.customConfig);
            log.debug("updateUserConfig_更新成功: {}", jsonObject);
        }
    }

    public Object findUserConfig(Long userId) {
        //add cache
        String cacheKey = NoteCacheKey.NOTE_USER_CONFIG_KEY+userId;
        Object cVal = cacheService.get(cacheKey);
        if (cVal != null) {
            return cVal;
        }
        Document doc = mongoTemplate.findOne(Query.query(Criteria.where(NoteConstants.userid).is(userId)), Document.class, NoteConstants.customConfig);
        if (doc == null) {
            throw new BusinessException(CommonErrorCode.E_200212);
        }
        return doc.toJson();
    }
}
