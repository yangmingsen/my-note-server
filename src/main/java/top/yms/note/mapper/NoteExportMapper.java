package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NoteExport;
import top.yms.note.entity.NoteExportExample;

import java.util.List;

@Mapper
public interface NoteExportMapper {
    long countByExample(NoteExportExample example);

    int deleteByExample(NoteExportExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteExport record);

    int insertSelective(NoteExport record);

    List<NoteExport> selectByExample(NoteExportExample example);

    NoteExport selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteExport record, @Param("example") NoteExportExample example);

    int updateByExample(@Param("record") NoteExport record, @Param("example") NoteExportExample example);

    int updateByPrimaryKeySelective(NoteExport record);

    int updateByPrimaryKey(NoteExport record);
}