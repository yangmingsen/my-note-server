package top.yms.note.mapper.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.note.comm.NoteConstants;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by yangmingsen on 2024/10/13.
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(String.class)
public class FileUrlTypeHandler extends BaseTypeHandler<String> {

    private final static Logger log = LoggerFactory.getLogger(FileUrlTypeHandler.class);

    private String getBaseUrlPrefix() {
        return NoteConstants.getBaseUrl();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        log.debug("idx={}, parameter={}", i, parameter);
        ps.setString(i, parameter);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getBaseUrlPrefix()+rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getBaseUrlPrefix()+rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getBaseUrlPrefix()+cs.getString(columnIndex);
    }
}
