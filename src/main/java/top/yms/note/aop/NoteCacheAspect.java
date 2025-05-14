package top.yms.note.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;

import javax.annotation.Resource;
import java.util.Arrays;


@Aspect
@Component
public class NoteCacheAspect {
    private static final Logger log = LoggerFactory.getLogger(NoteCacheAspect.class);

    @Value("${system.enable-service-cache}")
    private boolean enableCache;

    @Resource
    @Qualifier(NoteConstants.noteExpireTimeCache)
    private NoteCacheService noteCacheService;

    @Pointcut("execution(* top.yms.note.service.impl..*(..))")
    public void cacheMethods() {}

    private boolean canApply(String methodName) {
        String [] shouldCache = {"find","get"};
        for (String sc : shouldCache) {
            if (methodName.startsWith(sc)) {
                return true;
            }
        }
        return false;
    }

    @Around("cacheMethods()")
    public Object cacheAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (enableCache) {
            String methodName = joinPoint.getSignature().getName();
            String targetName = joinPoint.getTarget().getClass().getName();
            if (!canApply(methodName)) {
                return joinPoint.proceed(); // 不缓存其他方法
            }
            String key = NoteConstants.C_METHOD + targetName+"#"+methodName + Arrays.toString(joinPoint.getArgs());
            Object cached = noteCacheService.find(key);
            if (cached != null) {
                log.debug("cache hit for key={}", key);
                return cached;
            }
            Object result = joinPoint.proceed();
            noteCacheService.update(key, result);
            log.debug("cache store for key={}", key);
            return result;
        }
        return joinPoint.proceed();
    }
}
