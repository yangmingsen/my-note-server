package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import top.yms.note.entity.FileStoreRelation;
import top.yms.note.entity.FileStoreRelationExample;

import java.util.List;

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

    @Select("select f_id, f_mongo_file_id, f_storage_file_id, f_note_file_id, f_create_time from t_file_store_relation where f_mongo_file_id = #{mongoFileId, jdbcType=VARCHAR}")
    @ResultMap("BaseResultMap")
    FileStoreRelation selectByMongoFileId(@Param("mongoFileId") String mongoFileId);


    @Select("select f_id, f_mongo_file_id, f_storage_file_id, f_note_file_id, f_create_time from t_file_store_relation where f_note_file_id = #{noteFileId, jdbcType=VARCHAR}")
    @ResultMap("BaseResultMap")
    FileStoreRelation selectByNoteFileId(@Param("noteFileId") String noteFileId);



}