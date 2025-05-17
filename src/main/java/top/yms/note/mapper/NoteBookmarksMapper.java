package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteBookmarks;
import top.yms.note.entity.NoteBookmarksExample;

@Mapper
public interface NoteBookmarksMapper {
    long countByExample(NoteBookmarksExample example);

    int deleteByExample(NoteBookmarksExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteBookmarks record);

    int insertSelective(NoteBookmarks record);

    List<NoteBookmarks> selectByExample(NoteBookmarksExample example);

    NoteBookmarks selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteBookmarks record, @Param("example") NoteBookmarksExample example);

    int updateByExample(@Param("record") NoteBookmarks record, @Param("example") NoteBookmarksExample example);

    int updateByPrimaryKeySelective(NoteBookmarks record);

    int updateByPrimaryKey(NoteBookmarks record);
}