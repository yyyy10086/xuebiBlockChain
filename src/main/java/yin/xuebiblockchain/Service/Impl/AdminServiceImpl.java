package yin.xuebiblockchain.Service.Impl;

import org.springframework.stereotype.Service;
import yin.xuebiblockchain.Mapper.NewsMapper;
import yin.xuebiblockchain.Mapper.ResourceMapper;
import yin.xuebiblockchain.Mapper.PostMapper;
import yin.xuebiblockchain.Pojo.NewsDTO;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.Resource;
import yin.xuebiblockchain.Service.AdminService;
import yin.xuebiblockchain.Utils.UserHolder;
import yin.xuebiblockchain.Pojo.UserDTO;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @jakarta.annotation.Resource
    private NewsMapper newsMapper;

    @jakarta.annotation.Resource
    private ResourceMapper resourceMapper;

    @jakarta.annotation.Resource
    private PostMapper postMapper;

    @Override
    public Result addNews(NewsDTO news) {
        UserDTO admin = UserHolder.getUser();
        news.setAuthorId(admin.getId());
        news.setAuthorName(admin.getNickName());
        int rows = newsMapper.insertNews(news);
        if (rows > 0) return Result.success("新闻发布成功");
        return Result.error("新闻发布失败");
    }

    @Override
    public Result updateNews(NewsDTO news) {
        int rows = newsMapper.updateNews(news);
        if (rows > 0) return Result.success("新闻更新成功");
        return Result.error("新闻更新失败");
    }

    @Override
    public Result deleteNews(Long id) {
        int rows = newsMapper.deleteNews(id);
        if (rows > 0) return Result.success("新闻删除成功");
        return Result.error("新闻删除失败");
    }

    @Override
    public Result listPendingResources() {
        List<Resource> resources = resourceMapper.findPendingResources();
        return Result.success(resources);
    }

    @Override
    public Result auditResource(Long id, String status) {
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return Result.error("审核状态无效");
        }
        int rows = resourceMapper.updateAuditStatus(id, status);
        if (rows > 0) return Result.success("审核完成");
        return Result.error("审核失败");
    }

    @Override
    public Result forceOfflineResource(Long id) {
        Resource resource = resourceMapper.findById(id);
        if (resource == null) return Result.error("资源不存在");
        int rows = resourceMapper.updateResourceStatus(id, Resource.STATUS_OFFLINE, null);
        if (rows > 0) return Result.success("资源已强制下架");
        return Result.error("下架失败");
    }

    @Override
    public Result deletePost(Long postId) {
        int rows = postMapper.logicDeletePost(postId);
        if (rows > 0) return Result.success("帖子已删除");
        return Result.error("删除失败");
    }
}