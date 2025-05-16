package top.yms.note.conpont.task;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.SensitiveService;
import top.yms.note.enums.AsyncTaskEnum;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangmingsen on 2024/10/3.
 */
@Component
public class UserConfigSyncTask extends AbstractAsyncExecuteTask implements ScheduledExecuteTask{

    private final static Logger log = LoggerFactory.getLogger(UserConfigSyncTask.class);

    private final static String lastvisit = "lastvisit";

    @Resource
    private SensitiveService sensitiveService;

    @Value("${user-config.task.immediately}")
    private int immediatelySize;

    public int getSortValue() {
        return 1;
    }

    public void afterAddTask(AsyncTask task) {
        int dataSize = getDataSize();
        log.debug("curDataSize={}, immediatelySize={}",dataSize, immediatelySize );
        if (dataSize >= immediatelySize) {
            doRun();
        }
    }

    @Override
    boolean needTx() {
        return false;
    }

    @Override
    void doRun() {
        List<AsyncTask> allData = getAllData();
//        log.info("当前时间: {} , 获取到数据: {}", DateHelper.getYYYY_MM_DD_HH_MM_SS(), allData);
        for(AsyncTask asyncTask : allData) {
            JSONObject userConfig = new JSONObject();
            JSONObject tmpJson = (JSONObject) asyncTask.getTaskInfo();
            for (Map.Entry<String, Object>  entry: tmpJson.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                userConfig.put(key, value);
            }
            JSONObject lastV = tmpJson.getJSONObject(lastvisit);
            if (lastV != null) {
                String lastVid = lastV.getString(NoteConstants.id);
                if (StringUtils.isNotBlank(lastVid))  {
                    if (sensitiveService.isSensitive(Long.parseLong(lastVid))) {
                        //命中敏感内容
                        log.debug("命中敏感内容 id={}", lastVid);
                        continue;
                    }
                }
            }
            Long userId = asyncTask.getUserId();
            Document oldDoc = mongoTemplate.findOne(Query.query(Criteria.where(NoteConstants.userid).is(userId)), Document.class, NoteConstants.customConfig);
            if (oldDoc == null) {
                userConfig.put(NoteConstants.userid, userId);
                Document newDoc = Document.parse(userConfig.toString());
                mongoTemplate.save(newDoc, NoteConstants.customConfig);
            } else {
                for (Map.Entry<String, Object> entry : userConfig.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    oldDoc.put(key, value);
                }
                mongoTemplate.save(oldDoc,  NoteConstants.customConfig);
                log.debug("updateUserConfig_更新成功: {}", oldDoc.toJson());
            }
        }

    }

    @Override
    public boolean support(AsyncTask task) {
        return AsyncTaskEnum.apply(task.getType().getValue()) == AsyncTaskEnum.SYNC_USER_CONFIG;
    }


    @Override
    public void regScheduledTask(NoteScheduledExecutorService noteScheduledExecuteService) {
        noteScheduledExecuteService.scheduleWithFixedDelay(this, 5, 30, TimeUnit.SECONDS);
        log.info("UserConfigSyncTask注册到ScheduledTask成功...");
    }

}
