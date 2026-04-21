package yin.xuebiblockchain.Pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String userName;          // 发帖人昵称
    private String userIcon;      // 头像 URL
    private String postTitle;
    private String postContent;
    private LocalDateTime updateTime;  // 对应数据库 update_time
    private Integer likeCount;
    private Integer viewCount;
    private Integer shareCount;
    private Integer commentCount;
    private Boolean isLiked;           // 当前用户是否已点赞
}