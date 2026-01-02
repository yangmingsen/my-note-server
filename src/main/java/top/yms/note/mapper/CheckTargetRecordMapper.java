package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.CheckTargetRecord;
import top.yms.note.entity.CheckTargetRecordExample;

import java.util.List;

@Mapper
public interface CheckTargetRecordMapper {
    long countByExample(CheckTargetRecordExample example);

    int deleteByExample(CheckTargetRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CheckTargetRecord record);

    int insertSelective(CheckTargetRecord record);

    List<CheckTargetRecord> selectByExample(CheckTargetRecordExample example);

    CheckTargetRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CheckTargetRecord record, @Param("example") CheckTargetRecordExample example);

    int updateByExample(@Param("record") CheckTargetRecord record, @Param("example") CheckTargetRecordExample example);

    int updateByPrimaryKeySelective(CheckTargetRecord record);

    int updateByPrimaryKey(CheckTargetRecord record);
}