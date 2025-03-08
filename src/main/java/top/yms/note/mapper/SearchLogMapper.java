package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.SearchLog;
import top.yms.note.entity.SearchLogExample;

import java.util.List;

@Mapper
public interface SearchLogMapper {
    long countByExample(SearchLogExample example);

    int deleteByExample(SearchLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SearchLog record);

    int insertSelective(SearchLog record);

    List<SearchLog> selectByExample(SearchLogExample example);

    SearchLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SearchLog record, @Param("example") SearchLogExample example);

    int updateByExample(@Param("record") SearchLog record, @Param("example") SearchLogExample example);

    int updateByPrimaryKeySelective(SearchLog record);

    int updateByPrimaryKey(SearchLog record);
}