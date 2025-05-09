package top.yms.note.config;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.note.comm.NoteSystemErrorCode;
import top.yms.note.comm.NoteSystemException;

import java.util.Properties;
import java.util.concurrent.Executor;

@Intercepts({
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        )
})
//@Component
public class AffectedRowsInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(AffectedRowsInterceptor.class);
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();

        if (result instanceof Integer) {
            int rows = (Integer) result;
            MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
            String methodId = ms.getId(); // 获取具体是哪个 Mapper 方法
            if (rows == 0) {
                log.error("数据库操作无效：" + methodId + " 未影响任何行");
                throw new NoteSystemException( NoteSystemErrorCode.E_400000);
            }
            log.debug("methodId={} affected rows={}", methodId, rows);
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可通过配置传参，这里可忽略
    }
}