package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import top.yms.note.entity.NoteUser;
import top.yms.note.entity.NoteUserExample;

import java.util.List;

@Mapper
public interface NoteUserMapper {
    long countByExample(NoteUserExample example);

    int deleteByExample(NoteUserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteUser record);

    int insertSelective(NoteUser record);

    List<NoteUser> selectByExample(NoteUserExample example);

    NoteUser selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteUser record, @Param("example") NoteUserExample example);

    int updateByExample(@Param("record") NoteUser record, @Param("example") NoteUserExample example);

    int updateByPrimaryKeySelective(NoteUser record);

    int updateByPrimaryKey(NoteUser record);

    @Select("select * from t_note_user where f_username=#{username, jdbcType=VARCHAR}")
    @ResultMap("BaseResultMap")
    NoteUser selectByUserName(@Param("username") String username);
}