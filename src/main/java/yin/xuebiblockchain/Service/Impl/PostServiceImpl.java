package yin.xuebiblockchain.Service.Impl;

import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yin.xuebiblockchain.Mapper.PostMapper;
import yin.xuebiblockchain.Pojo.Post;
import yin.xuebiblockchain.Pojo.PostVO;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Service.PostService;
import yin.xuebiblockchain.Utils.UserHolder;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Resource
    private PostMapper postMapper;

    @Override
    public Result getPostList(int page, int size) {
        Long currentUserId = null;
        if (UserHolder.getUser() != null) {
            currentUserId = UserHolder.getUser().getId();
        }
        int offset = (page - 1) * size;
        List<PostVO> list = postMapper.selectPostListWithDetails(offset, size, currentUserId);
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result sendPost(String postContent) {
        Long userId = UserHolder.getUser().getId();
        Post post = new Post();
        post.setUserId(userId);
        post.setPostContent(postContent);
        post.setPostTitle(postContent.length() > 20 ? postContent.substring(0, 20) : postContent);
        postMapper.insertPost(post);
        Long postId = post.getId();
        postMapper.insertPostDataBatch(postId);
        return Result.success(postId);
    }

    @Override
    public Result isView(long postId) {
        postMapper.incrementPostData(postId, "02", 1);
        return Result.success();
    }

    @Override
    @Transactional
    public Result isLike(long postId) {
        Long userId = UserHolder.getUser().getId();
        try {
            postMapper.insertUserLike(userId, postId);
            postMapper.incrementPostData(postId, "01", 1);
        } catch (DuplicateKeyException e) {
            // 已点赞，取消点赞
            postMapper.deleteUserLike(userId, postId);
            postMapper.incrementPostData(postId, "01", -1);
        }
        return Result.success();
    }

    @Override
    public Result isShare(long postId) {
        postMapper.incrementPostData(postId, "03", 1);
        return Result.success();
    }
}