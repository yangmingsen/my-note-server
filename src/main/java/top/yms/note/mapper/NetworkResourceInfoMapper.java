package top.yms.note.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.note.entity.NetworkResourceInfo;
import top.yms.note.entity.NetworkResourceInfoExample;

@Mapper
public interface NetworkResourceInfoMapper {
    long countByExample(NetworkResourceInfoExample example);

    int deleteByExample(NetworkResourceInfoExample example);

    int deleteByPrimaryKey(String noteFileId);

    int insert(NetworkResourceInfo record);

    int insertBatch(@Param("list") List<NetworkResourceInfo> list);

    int insertSelective(NetworkResourceInfo record);

    List<NetworkResourceInfo> selectByExample(NetworkResourceInfoExample example);

    NetworkResourceInfo selectByPrimaryKey(String noteFileId);

    int updateByExampleSelective(@Param("record") NetworkResourceInfo record, @Param("example") NetworkResourceInfoExample example);

    int updateByExample(@Param("record") NetworkResourceInfo record, @Param("example") NetworkResourceInfoExample example);

    int updateByPrimaryKeySelective(NetworkResourceInfo record);

    int updateByPrimaryKey(NetworkResourceInfo record);
}