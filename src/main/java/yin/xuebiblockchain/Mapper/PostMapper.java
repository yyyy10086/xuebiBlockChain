package yin.xuebiblockchain.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import yin.xuebiblockchain.Pojo.Post;
import yin.xuebiblockchain.Pojo.PostVO;
import java.util.List;

@Mapper
public interface PostMapper {

    List<PostVO> selectPostListWithDetails(@Param("offset") int offset,
                                           @Param("size") int size,
                                           @Param("currentUserId") Long currentUserId);

    int insertPost(Post post);

    int insertPostDataBatch(@Param("postId") Long postId);

    int incrementPostData(@Param("postId") long postId,
                          @Param("status") String status,
                          @Param("delta") int delta);

    Integer getPostDataNumber(@Param("postId") long postId,
                              @Param("status") String status);

    int insertUserLike(@Param("userId") Long userId, @Param("postId") long postId);

    int deleteUserLike(@Param("userId") Long userId, @Param("postId") long postId);
}