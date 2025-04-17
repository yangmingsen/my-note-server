package top.yms.note.config;

/**
 * Created by yangmingsen on 2022/11/3.
 */
//import com.baomidou.mybatisplus.core.toolkit.StringPool;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.comm.NoteSystemErrorCode;
import top.yms.note.comm.NoteSystemException;
import top.yms.note.conpont.SysConfigService;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * @author lww
 * @date 2020-09-01 00:13
 */
@Intercepts(
        {
            @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
            @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
        }
)

@Component
public class PrintSqlInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(PrintSqlInterceptor.class);

    @Value("${system.sql-show}")
    private String sqlShow;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        String sqlId = mappedStatement.getId();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        long start = System.currentTimeMillis();
        Object returnValue = invocation.proceed();
        long time = System.currentTimeMillis() - start;
        if (StringUtils.isNotBlank(sqlShow) && sqlShow.contains("true")) {
            showSql(configuration, boundSql, time, sqlId);
        }
        if (returnValue instanceof Integer) {
            int rows = (Integer) returnValue;
            MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
            String methodId = ms.getId(); // 获取具体是哪个 Mapper 方法
            if (rows == 0) {
                log.info("数据库操作无效：" + methodId + " 未影响任何行");
                throw new NoteSystemException( NoteSystemErrorCode.E_400000);
            }
            log.info("methodId={} affected rows= {}", methodId, rows);
        }
        return returnValue;
    }

    private static void showSql(Configuration configuration, BoundSql boundSql, long time, String sqlId) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        //替换空格、换行、tab缩进等
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        logs(time, sql, sqlId);
    }

    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        String res =  value.replace("$", "\\$");
        if (res.length() > 50) {
            return res.substring(0, 50)+"...'";
        }
        return res;
    }

    private static void logs(long time, String sql, String sqlId) {
        StringBuilder sb = new StringBuilder()
                .append(" Time：").append(time)
                .append(" ms - ID：").append(sqlId)
                .append("\n").append("Execute SQL：")
                .append(sql);//.append("\n");
        log.info(sb.toString());
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties0) {
    }
}
