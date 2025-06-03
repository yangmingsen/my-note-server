package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.FileStoreRelation;
import top.yms.note.entity.FileStoreRelationExample;

@Mapper
public interface FileStoreRelationMapper {
    long countByExample(FileStoreRelationExample example);

    int deleteByExample(FileStoreRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FileStoreRelation record);

    int insertSelective(FileStoreRelation record);

    List<FileStoreRelation> selectByExample(FileStoreRelationExample example);

    FileStoreRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FileStoreRelation record, @Param("example") FileStoreRelationExample example);

    int updateByExample(@Param("record") FileStoreRelation record, @Param("example") FileStoreRelationExample example);

    int updateByPrimaryKeySelective(FileStoreRelation record);

    int updateByPrimaryKey(FileStoreRelation record);
}