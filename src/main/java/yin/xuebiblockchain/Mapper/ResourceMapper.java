package yin.xuebiblockchain.Mapper;

import java.sql.Timestamp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import yin.xuebiblockchain.Pojo.Resource;
import yin.xuebiblockchain.Pojo.ResourceDTO;

import java.util.List;

/**
 * 资源数据库操作接口
 *
 * 【注意】不继承 BaseMapper，所有 SQL 都在 ResourceMapper.xml 中定义。
 * 这样和项目中其他 Mapper（TransactionMapper、PostMapper 等）风格一致。
 */
@Mapper
public interface ResourceMapper {

    /**
     * 根据ID查询资源（替代 BaseMapper.selectById）
     */
    Resource findById(@Param("id") Long id);

    List<ResourceDTO> findAvailableResources();

    List<ResourceDTO> findByCategory(@Param("category") String category);

    List<Resource> findByOwnerId(@Param("ownerId") Long ownerId);

    List<ResourceDTO> findBorrowedByUser(@Param("borrowerId") Long borrowerId);

    int insertResource(Resource resource);

    int updateResourceStatus(@Param("id") Long id,
                             @Param("status") String status,
                             @Param("borrowerId") Long borrowerId);

    int incrementBorrowCount(@Param("id") Long id);

    List<ResourceDTO> findHotResources(@Param("limit") int limit);

    List<ResourceDTO> searchResources(@Param("keyword") String keyword);

    int updateResourceStatusAndBorrowEndTime(@Param("id") Long id,
                                             @Param("status") String status,
                                             @Param("borrowerId") Long borrowerId,
                                             @Param("borrowEndTime") Timestamp borrowEndTime);

    List<ResourceDTO> findAllResources();
}