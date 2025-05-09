package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteTagUser;
import top.yms.note.entity.NoteTagUserExample;

import java.util.List;

@Mapper
public interface NoteTagUserMapper {
    long countByExample(NoteTagUserExample example);

    int deleteByExample(NoteTagUserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteTagUser record);

    int insertSelective(NoteTagUser record);

    List<NoteTagUser> selectByExample(NoteTagUserExample example);

    NoteTagUser selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteTagUser record, @Param("example") NoteTagUserExample example);

    int updateByExample(@Param("record") NoteTagUser record, @Param("example") NoteTagUserExample example);

    int updateByPrimaryKeySelective(NoteTagUser record);

    int updateByPrimaryKey(NoteTagUser record);
}