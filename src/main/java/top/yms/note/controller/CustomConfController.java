package top.yms.note.controller;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteAsyncExecuteTaskService;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.conpont.task.AsyncTask;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.RestOut;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.service.NoteIndexService;
import top.yms.note.service.impl.CustomConfServiceImpl;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by yangmingsen on 2024/9/28.
 */
@RestController
@RequestMapping("/custom-conf")
public class CustomConfController {

    private final static Logger log = LoggerFactory.getLogger(CustomConfController.class);

    @Resource
    private CustomConfServiceImpl customConfServiceImpl;

    @Resource
    private NoteAsyncExecuteTaskService noteExecuteTaskService;

    @Resource
    private IdWorker idWorker;

    @Resource
    private NoteCacheService noteCacheService;

    @Resource
    private NoteIndexService noteIndexService;

    @PostMapping("/update-user-config")
    public RestOut updateUserConfig(@RequestParam("content") String jsonContent) {
        if (StringUtils.isBlank(jsonContent)) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        log.debug("updateUserConfig: jsonContent={}", jsonContent);
        JSONObject userConfigJsonData = JSONObject.parseObject(jsonContent);
        AsyncTask asyncTask = AsyncTask.Builder
                .build()
                .taskId(idWorker.nextId())
                .taskName(AsyncTaskEnum.SYNC_USER_CONFIG.getName())
                .createTime(new Date())
                .userId(LocalThreadUtils.getUserId())
                .type(AsyncTaskEnum.SYNC_USER_CONFIG)
                .executeType(AsyncExcuteTypeEnum.TIMED_TASK)
                .taskInfo(userConfigJsonData)
                .get();
        //如果使用bg更换需要,需要使用调用者线程
        if (userConfigJsonData.containsKey(NoteConstants.bgImgInfo)) {
            asyncTask.setExecuteType(AsyncExcuteTypeEnum.CALLER_TASK);
        }
        noteExecuteTaskService.addTask(asyncTask);
        //关于lru计算
        if (userConfigJsonData.containsKey(NoteConstants.lastvisit)) {
            JSONObject lastVisitJson = userConfigJsonData.getJSONObject(NoteConstants.lastvisit);
            String noteId = lastVisitJson.getString("id");
            NoteIndex noteMeta = noteIndexService.findOne(Long.valueOf(noteId));
            AsyncTask visitComputeTask = AsyncTask.Builder
                    .build()
                    .taskId(idWorker.nextId())
                    .taskName(AsyncTaskEnum.SYNC_COMPUTE_RECENT_VISIT.getName())
                    .createTime(new Date())
                    .userId(LocalThreadUtils.getUserId())
                    .type(AsyncTaskEnum.SYNC_COMPUTE_RECENT_VISIT)
                    .executeType(AsyncExcuteTypeEnum.SYNC_TASK)
                    .taskInfo(noteMeta)
                    .get();
            noteExecuteTaskService.addTask(visitComputeTask);
        }
        return RestOut.succeed();
    }

    @GetMapping("/find-user-conf")
    public RestOut<Object> findUserConfig() {
        return RestOut.success(customConfServiceImpl.findUserConfig(LocalThreadUtils.getUserId()));
    }

}
