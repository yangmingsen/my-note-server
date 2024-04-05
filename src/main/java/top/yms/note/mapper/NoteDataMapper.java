package top.yms.note.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataExample;

public interface NoteDataMapper {
    long countByExample(NoteDataExample example);

    int deleteByExample(NoteDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteData record);

    int insertSelective(NoteData record);

    List<NoteData> selectByExampleWithBLOBs(NoteDataExample example);

    List<NoteData> selectByExample(NoteDataExample example);

    NoteData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteData record, @Param("example") NoteDataExample example);

    int updateByExampleWithBLOBs(@Param("record") NoteData record, @Param("example") NoteDataExample example);

    int updateByExample(@Param("record") NoteData record, @Param("example") NoteDataExample example);

    int updateByPrimaryKeySelective(NoteData record);

    int updateByPrimaryKeyWithBLOBs(NoteData record);

    int updateByPrimaryKey(NoteData record);
}