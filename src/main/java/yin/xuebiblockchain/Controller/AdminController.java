package yin.xuebiblockchain.Controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import yin.xuebiblockchain.Pojo.NewsDTO;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.UserDTO;
import yin.xuebiblockchain.Service.AdminService;
import yin.xuebiblockchain.Utils.UserHolder;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminService adminService;

    // 校验管理员身份
    private Result checkAdmin() {
        UserDTO user = UserHolder.getUser();
        if (user == null) return Result.error("请先登录");
        if (user.getRole() == null || user.getRole() != 1) return Result.error("无管理员权限");
        return null;
    }

    // ========== 新闻管理 ==========
    @PostMapping("/news")
    public Result addNews(@RequestBody NewsDTO news) {
        Result check = checkAdmin();
        if (check != null) return check;
        return adminService.addNews(news);
    }

    @PutMapping("/news/{id}")
    public Result updateNews(@PathVariable Long id, @RequestBody NewsDTO news) {
        Result check = checkAdmin();
        if (check != null) return check;
        news.setId(id);
        return adminService.updateNews(news);
    }

    @DeleteMapping("/news/{id}")
    public Result deleteNews(@PathVariable Long id) {
        Result check = checkAdmin();
        if (check != null) return check;
        return adminService.deleteNews(id);
    }

    // ========== 资源审核 ==========
    @GetMapping("/resources/pending")
    public Result listPendingResources() {
        Result check = checkAdmin();
        if (check != null) return check;
        return adminService.listPendingResources();
    }

    @PutMapping("/resource/{id}/audit")
    public Result auditResource(@PathVariable Long id, @RequestParam String status) {
        Result check = checkAdmin();
        if (check != null) return check;
        return adminService.auditResource(id, status);
    }

    // 强制下架任何资源
    @PutMapping("/resource/{id}/offline")
    public Result forceOfflineResource(@PathVariable Long id) {
        Result check = checkAdmin();
        if (check != null) return check;
        return adminService.forceOfflineResource(id);
    }

    // ========== 帖子管理 ==========
    @DeleteMapping("/post/{postId}")
    public Result deletePost(@PathVariable Long postId) {
        Result check = checkAdmin();
        if (check != null) return check;
        return adminService.deletePost(postId);
    }
}
