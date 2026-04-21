package yin.xuebiblockchain.Service;

import yin.xuebiblockchain.Pojo.Result;

public interface PostService {
    Result getPostList(int page, int size);
    Result sendPost(String postContent);
    Result isLike(long postId);
    Result isView(long postId);
    Result isShare(long postId);
}