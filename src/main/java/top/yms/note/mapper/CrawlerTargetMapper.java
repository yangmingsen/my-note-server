package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import top.yms.note.entity.CrawlerTarget;
import top.yms.note.entity.CrawlerTargetExample;

@Mapper
public interface CrawlerTargetMapper {
    long countByExample(CrawlerTargetExample example);

    int deleteByExample(CrawlerTargetExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CrawlerTarget record);

    int insertSelective(CrawlerTarget record);

    List<CrawlerTarget> selectByExample(CrawlerTargetExample example);

    CrawlerTarget selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CrawlerTarget record, @Param("example") CrawlerTargetExample example);

    int updateByExample(@Param("record") CrawlerTarget record, @Param("example") CrawlerTargetExample example);

    int updateByPrimaryKeySelective(CrawlerTarget record);

    int updateByPrimaryKey(CrawlerTarget record);

    @Select("select f_id, f_url, f_condition, f_open, f_create_time,f_update_time from t_crawler_target where f_open = #{open, jdbcType=CHAR}")
    @ResultMap("BaseResultMap")
    List<CrawlerTarget> selectByOpenFlg(@Param("open") String open);
}