package yin.xuebiblockchain.Service;

import yin.xuebiblockchain.Pojo.Resource;
import yin.xuebiblockchain.Pojo.Result;

public interface Resourceservice {
    /**
     * 发布新资源
     * @param resource 资源信息（名称、描述、类型、积分定价等）
     * @param userId   当前登录用户的ID（自动设为分享者）
     * @return 成功返回资源ID，失败返回错误信息
     */
    Result publishResource(Resource resource, Long userId);

    /**
     * 查询所有可借用的资源（首页展示用）
     */
    Result listAvailableResources();

    /**
     * 查询热门资源（按借用次数排序，取前N个）
     * @param limit 返回数量
     */
    Result listHotResources(int limit);

    /**
     * 按分类查询资源
     * @param category 分类名称（教材/学习笔记/电子设备等）
     */
    Result listByCategory(String category);

    /**
     * 查询某个用户发布的所有资源（"我的分享"）
     */
    Result listMyResources(Long userId);

    /**
     * 查询某个用户正在借用的资源（"我的借用"）
     */
    Result listMyBorrowedResources(Long userId);

    /**
     * 根据资源ID查询资源详情
     */
    Result getResourceDetail(Long resourceId);

    /**
     * 关键词搜索资源
     */
    Result searchResources(String keyword);

    /**
     * 下架资源（只有分享者本人可以操作）
     */
    Result offlineResource(Long resourceId, Long userId);

    /**
     * 查询资源的流转历史（溯源功能核心）
     * @param resourceId 资源ID
     * @return 该资源从发布到现在的所有借用/归还记录
     */
    Result getResourceTrace(Long resourceId);

    Result listAllResources();
}
