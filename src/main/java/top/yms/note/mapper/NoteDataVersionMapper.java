package top.yms.note.mapper;

import org.apache.ibatis.annotations.*;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteDataVersionExample;

import java.util.List;

@Mapper
public interface NoteDataVersionMapper {
    long countByExample(NoteDataVersionExample example);

    int deleteByExample(NoteDataVersionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteDataVersion record);

    int insertSelective(NoteDataVersion record);

    List<NoteDataVersion> selectByExample(NoteDataVersionExample example);

    NoteDataVersion selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteDataVersion record, @Param("example") NoteDataVersionExample example);

    int updateByExample(@Param("record") NoteDataVersion record, @Param("example") NoteDataVersionExample example);

    int updateByPrimaryKeySelective(NoteDataVersion record);

    int updateByPrimaryKey(NoteDataVersion record);

    @Delete("delete from t_note_data_version where f_note_id=#{noteId, jdbcType=BIGINT}")
    int deleteByNoteId(@Param("noteId") Long noteId);

    @Select("select * from t_note_data_version where f_note_id=#{noteId, jdbcType=BIGINT}")
    @ResultMap("BaseResultMap")
    List<NoteDataVersion> selectByNoteId(@Param("noteId") Long noteId);
}