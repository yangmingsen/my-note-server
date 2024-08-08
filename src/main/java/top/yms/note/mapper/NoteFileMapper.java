package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteFileExample;

@Mapper
public interface NoteFileMapper {
    long countByExample(NoteFileExample example);

    int deleteByExample(NoteFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteFile record);

    int insertSelective(NoteFile record);

    List<NoteFile> selectByExample(NoteFileExample example);

    NoteFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteFile record, @Param("example") NoteFileExample example);

    int updateByExample(@Param("record") NoteFile record, @Param("example") NoteFileExample example);

    int updateByPrimaryKeySelective(NoteFile record);

    int updateByPrimaryKey(NoteFile record);

    @Delete("delete from t_note_file where f_file_id=#{fileId, jdbcType=VARCHAR}")
    int deleteByFileId(@Param("fileId") String fileId);
}