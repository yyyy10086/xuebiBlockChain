package yin.xuebiblockchain.Controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.UserDTO;
import yin.xuebiblockchain.Service.Resourceservice;
import yin.xuebiblockchain.Utils.UserHolder;

@Slf4j
@RestController
@RequestMapping("/resource")
public class ResourceController {
    @Resource
    private Resourceservice resourceService;

    /**
     * 查询所有可借用的资源
     * 首页的"热门共享资源"列表调用此接口
     */
    @GetMapping("/list")
    public Result listResources() {
        return resourceService.listAllResources();  // 改为返回所有资源
    }

    /**
     * 查询热门资源（按借用次数排序）
     * @param limit 返回数量，默认5
     */
    @GetMapping("/hot")
    public Result hotResources(@RequestParam(defaultValue = "5") int limit) {
        return resourceService.listHotResources(limit);
    }

    /**
     * 按分类查询资源
     * @param category 分类名称：教材 / 学习笔记 / 电子设备 / 体育器材 / 日常用品 / 其他
     */
    @GetMapping("/category")
    public Result listByCategory(@RequestParam String category) {
        return resourceService.listByCategory(category);
    }

    /**
     * 关键词搜索资源
     */
    @GetMapping("/search")
    public Result searchResources(@RequestParam String keyword) {
        return resourceService.searchResources(keyword);
    }

    /**
     * 查询资源详情
     * @param id 资源ID
     */
    @GetMapping("/detail/{id}")
    public Result getResourceDetail(@PathVariable Long id) {
        return resourceService.getResourceDetail(id);
    }

    /**
     * 查询我发布的资源（需要登录）
     */
    @GetMapping("/my")
    public Result listMyResources() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.error("请先登录");
        }
        return resourceService.listMyResources(user.getId());
    }

    /**
     * 查询我正在借用的资源（需要登录）
     */
    @GetMapping("/borrowed")
    public Result listMyBorrowedResources() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.error("请先登录");
        }
        return resourceService.listMyBorrowedResources(user.getId());
    }

    /**
     * 发布新资源（需要登录）
     *
     * 【前端调用示例】
     * POST /api/resource/publish
     * Body: {
     *   "name": "高等数学（第七版）",
     *   "description": "九成新，有少量笔记",
     *   "resourceType": "PHYSICAL",
     *   "category": "教材",
     *   "pointsCost": 15
     * }
     */
    @PostMapping("/publish")
    public Result publishResource(@RequestBody yin.xuebiblockchain.Pojo.Resource resource) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.error("请先登录");
        }
        return resourceService.publishResource(resource, user.getId());
    }

    /**
     * 下架资源（只有分享者本人可以操作）
     */
    @PostMapping("/offline/{id}")
    public Result offlineResource(@PathVariable Long id) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.error("请先登录");
        }
        return resourceService.offlineResource(id, user.getId());
    }

    /**
     * 查询资源流转历史（溯源功能）
     *
     * 【论文重点功能】
     * 通过资源ID，可以查看这个资源从发布到现在的完整流转记录。
     * 每条记录都包含区块链哈希和区块号，可以验证数据未被篡改。
     *
     * 前端调用：GET /api/resource/trace/1
     * 返回：{ resource: {...}, traceRecords: [...], totalRecords: 5 }
     */
    @GetMapping("/trace/{id}")
    public Result getResourceTrace(@PathVariable Long id) {
        return resourceService.getResourceTrace(id);
    }

    @GetMapping("/all")
    public Result listAllResources() {
        return resourceService.listAllResources();
    }
}
