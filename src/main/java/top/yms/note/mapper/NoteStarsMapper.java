package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteStars;
import top.yms.note.entity.NoteStarsExample;

import java.util.List;

@Mapper
public interface NoteStarsMapper {
    long countByExample(NoteStarsExample example);

    int deleteByExample(NoteStarsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteStars record);

    int insertSelective(NoteStars record);

    List<NoteStars> selectByExample(NoteStarsExample example);

    NoteStars selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteStars record, @Param("example") NoteStarsExample example);

    int updateByExample(@Param("record") NoteStars record, @Param("example") NoteStarsExample example);

    int updateByPrimaryKeySelective(NoteStars record);

    int updateByPrimaryKey(NoteStars record);
}