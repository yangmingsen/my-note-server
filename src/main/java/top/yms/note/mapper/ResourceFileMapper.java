package top.yms.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.ResourceFile;
import top.yms.note.entity.ResourceFileExample;

import java.util.List;

@Mapper
public interface ResourceFileMapper {
    long countByExample(ResourceFileExample example);

    int deleteByExample(ResourceFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ResourceFile record);

    int insertSelective(ResourceFile record);

    List<ResourceFile> selectByExample(ResourceFileExample example);

    ResourceFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ResourceFile record, @Param("example") ResourceFileExample example);

    int updateByExample(@Param("record") ResourceFile record, @Param("example") ResourceFileExample example);

    int updateByPrimaryKeySelective(ResourceFile record);

    int updateByPrimaryKey(ResourceFile record);
}