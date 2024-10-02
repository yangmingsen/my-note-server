package top.yms.note.controller;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteAsyncExecuteTaskService;
import top.yms.note.conpont.NoteCache;
import top.yms.note.conpont.task.AsyncTask;
import top.yms.note.entity.NoteUser;
import top.yms.note.entity.RestOut;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.service.CustomConfService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import java.util.Date;

/**
 * Created by yangmingsen on 2024/9/28.
 */
@RestController
@RequestMapping("/custom-conf")
public class CustomConfController {

    private final static Logger log = LoggerFactory.getLogger(CustomConfController.class);

    @Autowired
    private CustomConfService customConfService;

    @Autowired
    private NoteAsyncExecuteTaskService noteExecuteTaskService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private NoteCache noteCache;

    @PostMapping("/update-user-config")
    public RestOut updateUserConfig(@RequestParam("content") String jsonContent) {
        if (StringUtils.isBlank(jsonContent)) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        log.info("updateUserConfig: jsonContent={}", jsonContent);

        JSONObject userConfigJsonData = JSONObject.parseObject(jsonContent);
        AsyncTask asyncTask = AsyncTask.Builder
                .build()
                .taskId(idWorker.nextId())
                .taskName(AsyncTaskEnum.ASYNC_USER_CONFIG.getName())
                .createTime(new Date())
                .userId(LocalThreadUtils.getUserId())
                .type(AsyncTaskEnum.ASYNC_USER_CONFIG)
                .executeType(AsyncExcuteTypeEnum.TIMED_TASK)
                .taskInfo(userConfigJsonData)
                .get();

        //如果使用bg更换需要,需要使用调用者线程
        if (userConfigJsonData.containsKey(Constants.bgImgInfo)) {
            asyncTask.setExecuteType(AsyncExcuteTypeEnum.CALLER_TASK);
        }

        noteExecuteTaskService.addTask(asyncTask);

        return RestOut.succeed("Ok");
    }

    @GetMapping("/find-user-conf")
    public RestOut<Object> findUserConfig() {
        return RestOut.success(customConfService.findUserConfig(LocalThreadUtils.getUserId()));
    }
}
