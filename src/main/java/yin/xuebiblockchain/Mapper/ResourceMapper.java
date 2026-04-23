package yin.xuebiblockchain.Mapper;

import java.sql.Timestamp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import yin.xuebiblockchain.Pojo.Resource;
import yin.xuebiblockchain.Pojo.ResourceDTO;

import java.util.List;

@Mapper
public interface ResourceMapper {

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

    List<Resource> findPendingResources();

    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") String status);

    int deleteById(@Param("id") Long id);
}