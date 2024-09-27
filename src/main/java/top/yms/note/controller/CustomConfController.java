package top.yms.note.controller;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.BusinessException;
import top.yms.note.service.CustomConfService;
import top.yms.note.utils.LocalThreadUtils;

/**
 * Created by yangmingsen on 2024/9/28.
 */
@RestController
@RequestMapping("/custom-conf")
public class CustomConfController {

    private final static Logger log = LoggerFactory.getLogger(CustomConfController.class);

    @Autowired
    private CustomConfService customConfService;

    @PostMapping("/update-user-config")
    public RestOut updateUserConfig(@RequestParam("content") String jsonContent) {
        if (StringUtils.isBlank(jsonContent)) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        log.info("updateUserConfig: jsonContent={}", jsonContent);
        customConfService.updateUserConfig(JSONObject.parseObject(jsonContent));

        return RestOut.succeed("Ok");
    }

    @GetMapping("/find-user-conf")
    public RestOut<Object> findUserConfig() {
        return RestOut.success(customConfService.findUserConfig(LocalThreadUtils.getUserId()));
    }
}
