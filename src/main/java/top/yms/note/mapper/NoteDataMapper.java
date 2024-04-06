package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataExample;

@Mapper
public interface NoteDataMapper {
    long countByExample(NoteDataExample example);

    int deleteByExample(NoteDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteData record);

    int insertSelective(NoteData record);

    List<NoteData> selectByExampleWithBLOBs(NoteDataExample example);

    List<NoteData> selectByExample(NoteDataExample example);

    NoteData selectByPrimaryKey(Long id);

    @Select("select f_id, f_user_id from t_note_data where f_id = #{id, jdbcType=BIGINT}")
    @Results({
            @Result(column = "f_id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "f_user_id", property = "userId", jdbcType = JdbcType.BIGINT),
    })
    NoteData findById(@Param("id") Long id);

    int updateByExampleSelective(@Param("record") NoteData record, @Param("example") NoteDataExample example);

    int updateByExampleWithBLOBs(@Param("record") NoteData record, @Param("example") NoteDataExample example);

    int updateByExample(@Param("record") NoteData record, @Param("example") NoteDataExample example);

    int updateByPrimaryKeySelective(NoteData record);

    int updateByPrimaryKeyWithBLOBs(NoteData record);

    int updateByPrimaryKey(NoteData record);
}