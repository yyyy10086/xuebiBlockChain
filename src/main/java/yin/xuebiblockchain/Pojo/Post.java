package yin.xuebiblockchain.Pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private Long userId;
    private String postTitle;
    private String postContent;
    private Integer isDeleted;
    private LocalDateTime creatTime;
    private LocalDateTime updateTime;
}