package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteIndexExample;

@Mapper
public interface NoteIndexMapper {
    long countByExample(NoteIndexExample example);

    int deleteByExample(NoteIndexExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteIndex record);

    int insertSelective(NoteIndex record);

    List<NoteIndex> selectByExample(NoteIndexExample example);

    NoteIndex selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteIndex record, @Param("example") NoteIndexExample example);

    int updateByExample(@Param("record") NoteIndex record, @Param("example") NoteIndexExample example);

    int updateByPrimaryKeySelective(NoteIndex record);

    int updateByPrimaryKey(NoteIndex record);

    int delByIds(@Param("ids") long [] ids);

    int delByListIds(@Param("ids") List<Long> ids);
}