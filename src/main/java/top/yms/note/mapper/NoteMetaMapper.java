package top.yms.note.mapper;

import org.apache.ibatis.annotations.*;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteMetaExample;

import java.util.List;

@Mapper
public interface NoteMetaMapper {
    long countByExample(NoteMetaExample example);

    int deleteByExample(NoteMetaExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NoteMeta record);

    int insertSelective(NoteMeta record);

    List<NoteMeta> selectByExample(NoteMetaExample example);

    NoteMeta selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NoteMeta record, @Param("example") NoteMetaExample example);

    int updateByExample(@Param("record") NoteMeta record, @Param("example") NoteMetaExample example);

    int updateByPrimaryKeySelective(NoteMeta record);

    int updateByPrimaryKey(NoteMeta record);

    int delByIds(@Param("ids") long [] ids);

    int delByListIds(@Param("ids") List<Long> ids);

    @Delete("delete from t_note_index where f_user_id = #{uid, jdbcType=BIGINT} and f_del=1")
    int allDestroy(@Param("uid") Long uid);


    @Select("select * from t_note_index where f_user_id = #{uid, jdbcType=BIGINT} and f_del=1")
    @ResultMap("BaseResultMap")
    List<NoteMeta> selectDestroyNotes(@Param("uid") Long uid);


    @Select("select * from t_note_index where f_parent_id = #{parentId, jdbcType=BIGINT} and f_del=0")
    @ResultMap("BaseResultMap")
    List<NoteMeta> selectByParentId(@Param("parentId") Long parentId);

    @Update("update t_note_index set f_del=0 where f_user_id = #{uid, jdbcType=BIGINT}")
    int allRecover(@Param("uid") Long uid);

    List<NoteMeta> selectRecentUpdate(@Param("userId") Long userId);

    @Select("select * from t_note_index where f_isfile='1' and f_user_id = #{userId, jdbcType=BIGINT} and f_name like concat('%',#{name, jdbcType=VARCHAR},'%')")
    @ResultMap("BaseResultMap")
    List<NoteMeta> searchName(@Param("name") String name, @Param("userId") Long userId);

    //应该要包含对目录的的查询
    @Select("select * from t_note_index where  f_del='0'")
    @ResultMap("BaseResultMap")
    List<NoteMeta> findAll();

    @Select("select * from t_note_index where f_user_id = #{userId, jdbcType=BIGINT}")
    @ResultMap("BaseResultMap")
    List<NoteMeta> selectByUserId(@Param("userId") Long userId);

    List<NoteMeta> selectByCondition(NoteMeta qry);
}