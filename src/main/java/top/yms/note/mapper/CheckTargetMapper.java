package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import top.yms.note.entity.CheckTarget;
import top.yms.note.entity.CheckTargetExample;

@Mapper
public interface CheckTargetMapper {
    long countByExample(CheckTargetExample example);

    int deleteByExample(CheckTargetExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CheckTarget record);

    int insertSelective(CheckTarget record);

    List<CheckTarget> selectByExample(CheckTargetExample example);

    CheckTarget selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CheckTarget record, @Param("example") CheckTargetExample example);

    int updateByExample(@Param("record") CheckTarget record, @Param("example") CheckTargetExample example);

    int updateByPrimaryKeySelective(CheckTarget record);

    int updateByPrimaryKey(CheckTarget record);

    @Select("select * from t_check_target")
    @ResultMap("BaseResultMap")
    List<CheckTarget> findAll();
}