package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import top.yms.note.entity.SystemConfig;
import top.yms.note.entity.SystemConfigExample;

@Mapper
public interface SystemConfigMapper {
    long countByExample(SystemConfigExample example);

    int deleteByExample(SystemConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SystemConfig record);

    int insertSelective(SystemConfig record);

    List<SystemConfig> selectByExample(SystemConfigExample example);

    SystemConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SystemConfig record, @Param("example") SystemConfigExample example);

    int updateByExample(@Param("record") SystemConfig record, @Param("example") SystemConfigExample example);

    int updateByPrimaryKeySelective(SystemConfig record);

    int updateByPrimaryKey(SystemConfig record);

    @Select("select * from t_system_config where f_config_key = #{configKey, jdbcType=VARCHAR}")
    @ResultMap("BaseResultMap")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    @Select("select * from t_system_config")
    @ResultMap("BaseResultMap")
    List<SystemConfig> findAll();
}