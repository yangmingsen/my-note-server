package top.yms.note.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.entity.RestOut;
import top.yms.note.exception.BusinessException;
import top.yms.note.exception.WangEditorUploadException;
import top.yms.note.msgcd.ErrorCode;

@Aspect
@Component
public class NoteControllerAspect {
    private final static Logger log = LoggerFactory.getLogger(NoteControllerAspect.class);

    @Pointcut("execution (* top.yms.note..*Controller.*(..))")
    public void anyController() {}

    @Around("anyController()")
    public Object doAroundController(ProceedingJoinPoint joinPoint) {
        try {
            Object resVal = joinPoint.proceed();
            return resVal;
//            if (resVal instanceof RestOut) {
//                //直接返回
//                return resVal;
//            }
//            return RestOut.success(resVal);
        } catch (WangEditorUploadException we) {
            throw we;
        } catch (BusinessException be) {
            ErrorCode errorCode = be.getErrorCode();
            //错误代码
            int code = errorCode.getCode();
            //错误信息
            String desc = errorCode.getDesc();
            log.error("BusinessException", be);
            return RestOut.error(code, desc);
        } catch (Throwable e) {
            log.error("系统未知异常：",e);
            //统一定义为99999系统未知错误
            return  RestOut.error(999999, e.getMessage()) ;
        }
    }
}
