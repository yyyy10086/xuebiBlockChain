package yin.xuebiblockchain.Service;
import yin.xuebiblockchain.Pojo.NewsDTO;
import yin.xuebiblockchain.Pojo.Result;

public interface AdminService {
    Result addNews(NewsDTO news);
    Result updateNews(NewsDTO news);
    Result deleteNews(Long id);
    Result listPendingResources();       // 查看待审核资源
    Result auditResource(Long id, String status);  // 审核资源（通过或拒绝）
    Result forceOfflineResource(Long id);          // 强制下架
    Result deletePost(Long postId);                // 删除帖子
}