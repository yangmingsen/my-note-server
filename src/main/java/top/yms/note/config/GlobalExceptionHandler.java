package top.yms.note.config;

/**
 * Created by yangmingsen on 2022/9/30.
 */

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.BusinessException;
import top.yms.note.exception.WangEditorUploadException;
import top.yms.note.msgcd.ErrorCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理器
 * @author Administrator
 * @version 1.0
 **/
@ControllerAdvice//与@Exceptionhandler配合使用实现全局异常处理
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //捕获Exception异常
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestOut processExcetion(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Exception e){
        //解析异常信息
        //如果是系统自定义异常，直接取出errCode和errMessage
        if(e instanceof BusinessException){
            LOGGER.info(e.getMessage(),e);
            //解析系统自定义异常信息
            BusinessException businessException= (BusinessException) e;
            ErrorCode errorCode = businessException.getErrorCode();
            //错误代码
            int code = errorCode.getCode();
            //错误信息
            String desc = errorCode.getDesc();
            return RestOut.error(code, desc);
        }

        LOGGER.error("系统异常：",e);
        //统一定义为99999系统未知错误
        return  RestOut.error(999999, e.getMessage()) ;
    }

    @ExceptionHandler(value = WangEditorUploadException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public JSONObject processLoginException(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Exception e) {

        LOGGER.error("wangEditor上传失败",e);
        JSONObject res = new JSONObject();
        //错误返回
        res.put("errno", 1);
        res.put("message", e.getMessage());
        return res;
    }
}
