package yin.xuebiblockchain.Service.Impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yin.xuebiblockchain.Mapper.ResourceMapper;
import yin.xuebiblockchain.Mapper.TransactionMapper;
import yin.xuebiblockchain.Mapper.UserMapper;
import yin.xuebiblockchain.Pojo.ResourceDTO;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Service.Resourceservice;
import yin.xuebiblockchain.Service.FabricCliService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ResourceServiceImpl implements Resourceservice {
    @Resource
    private ResourceMapper resourceMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TransactionMapper transactionMapper;

    @Resource
    private FabricCliService fabricCliService;

    /**
     * 发布新资源
     *
     * 【业务流程】
     * 1. 校验资源信息是否完整
     * 2. 设置分享者信息（从当前登录用户获取）
     * 3. 保存到数据库
     */
    @Override
    public Result publishResource(yin.xuebiblockchain.Pojo.Resource resource, Long userId) {
        // 参数校验
        if (resource.getName() == null || resource.getName().trim().isEmpty()) {
            return Result.error("资源名称不能为空");
        }
        if (resource.getPointsCost() == null || resource.getPointsCost() <= 0) {
            return Result.error("积分定价必须大于0");
        }
        if (resource.getResourceType() == null || resource.getResourceType().trim().isEmpty()) {
            return Result.error("请选择资源类型（电子文档/实体物品）");
        }

        // 设置分享者信息
        resource.setOwnerId(userId);

        // 查询用户的公钥地址
        String publicKey = userMapper.getPublicKeyById(userId);
        if (publicKey == null || publicKey.isEmpty()) {
            return Result.error("请先创建区块链账户");
        }
        resource.setOwnerAddress(publicKey);

        // 设置默认值
        if (resource.getCategory() == null || resource.getCategory().trim().isEmpty()) {
            resource.setCategory("其他");
        }
        resource.setStatus(yin.xuebiblockchain.Pojo.Resource.STATUS_AVAILABLE);
        resource.setBorrowCount(0);

        // 保存到数据库
        int rows = resourceMapper.insertResource(resource);
        if (rows > 0) {
            log.info("资源发布成功，ID: {}, 名称: {}, 分享者ID: {}", resource.getId(), resource.getName(), userId);

            // ========== 新增：将发布操作上链存证 ==========
            try {
                String metadata = "{}";
                String result = fabricCliService.publishResource(
                        String.valueOf(resource.getId()),
                        resource.getName(),
                        resource.getOwnerAddress(),
                        resource.getPointsCost(),
                        metadata
                );
                log.info("链码发布存证成功: {}", result);
            } catch (Exception e) {
                log.error("链码发布存证失败，但业务数据已提交。资源ID: {}", resource.getId(), e);
            }

            return Result.success(resource.getId());
        }
        return Result.error("资源发布失败，请重试");
    }

    /**
     * 查询所有可借用的资源
     */
    @Override
    public Result listAvailableResources() {
        // 原方法名虽叫 listAvailableResources，但实际应改为返回所有资源
        List<ResourceDTO> resources = resourceMapper.findAllResources(); // 需新加 Mapper 方法
        return Result.success(resources);
    }

    /**
     * 查询热门资源（按借用次数排序）
     */
    @Override
    public Result listHotResources(int limit) {
        if (limit <= 0) limit = 5;
        List<ResourceDTO> resources = resourceMapper.findHotResources(limit);
        return Result.success(resources);
    }

    /**
     * 按分类查询资源
     */
    @Override
    public Result listByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Result.error("请指定资源分类");
        }
        List<ResourceDTO> resources = resourceMapper.findByCategory(category);
        return Result.success(resources);
    }

    /**
     * 查询我发布的资源
     */
    @Override
    public Result listMyResources(Long userId) {
        List<yin.xuebiblockchain.Pojo.Resource> resources = resourceMapper.findByOwnerId(userId);
        return Result.success(resources);
    }

    /**
     * 查询我正在借用的资源
     */
    @Override
    public Result listMyBorrowedResources(Long userId) {
        List<ResourceDTO> resources = resourceMapper.findBorrowedByUser(userId);
        return Result.success(resources);
    }

    /**
     * 根据ID查询资源详情
     */
    @Override
    public Result getResourceDetail(Long resourceId) {
        if (resourceId == null) {
            return Result.error("资源ID不能为空");
        }
        yin.xuebiblockchain.Pojo.Resource resource = resourceMapper.findById(resourceId);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        return Result.success(resource);
    }

    /**
     * 关键词搜索资源
     */
    @Override
    public Result searchResources(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAvailableResources();
        }
        List<ResourceDTO> resources = resourceMapper.searchResources(keyword.trim());
        return Result.success(resources);
    }

    /**
     * 下架资源（只有分享者本人可以操作）
     */
    @Override
    public Result offlineResource(Long resourceId, Long userId) {
        yin.xuebiblockchain.Pojo.Resource resource = resourceMapper.findById(resourceId);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        if (!resource.getOwnerId().equals(userId)) {
            return Result.error("只有资源分享者本人可以下架资源");
        }
        if (yin.xuebiblockchain.Pojo.Resource.STATUS_LENT.equals(resource.getStatus())) {
            return Result.error("资源正在被借用中，无法下架");
        }
        resourceMapper.updateResourceStatus(resourceId,
                yin.xuebiblockchain.Pojo.Resource.STATUS_OFFLINE, null);
        log.info("资源已下架，ID: {}", resourceId);
        return Result.success("资源已下架");
    }

    /**
     * 查询资源的流转历史（溯源功能核心）
     *
     * 【这是论文中的重点功能】
     * 通过资源ID，可以查到从发布到现在的所有借用/归还记录，
     * 形成完整的流转链条。每条记录都有区块链哈希和区块号，
     * 可以和 LevelDB 中的区块链数据交叉验证，确保数据未被篡改。
     */
    @Override
    public Result getResourceTrace(Long resourceId) {
        if (resourceId == null) {
            return Result.error("资源ID不能为空");
        }
        // 查询资源基本信息
        yin.xuebiblockchain.Pojo.Resource resource = resourceMapper.findById(resourceId);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        // 查询该资源的所有交易记录（按时间正序 = 完整流转时间线）
        List<Map<String, Object>> traceRecords = transactionMapper.findByResourceId(resourceId);
        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("resource", resource);
        result.put("traceRecords", traceRecords);
        result.put("totalRecords", traceRecords.size());
        return Result.success(result);
    }

    @Override
    public Result listAllResources() {
        List<ResourceDTO> resources = resourceMapper.findAllResources();
        return Result.success(resources);
    }
}
