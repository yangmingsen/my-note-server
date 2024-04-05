package top.yms.note.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteUser;
import top.yms.note.entity.NoteUserExample;

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
}