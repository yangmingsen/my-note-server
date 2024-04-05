package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteIndexUpdateLog;
import top.yms.note.entity.NoteIndexUpdateLogExample;

@Mapper
public interface NoteIndexUpdateLogMapper {
    long countByExample(NoteIndexUpdateLogExample example);

    int deleteByExample(NoteIndexUpdateLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteIndexUpdateLog record);

    int insertSelective(NoteIndexUpdateLog record);

    int insertBatch(@Param("list") List<NoteIndexUpdateLog> list);

    List<NoteIndexUpdateLog> selectByExample(NoteIndexUpdateLogExample example);

    NoteIndexUpdateLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteIndexUpdateLog record, @Param("example") NoteIndexUpdateLogExample example);

    int updateByExample(@Param("record") NoteIndexUpdateLog record, @Param("example") NoteIndexUpdateLogExample example);

    int updateByPrimaryKeySelective(NoteIndexUpdateLog record);

    int updateByPrimaryKey(NoteIndexUpdateLog record);

}