package yin.xuebiblockchain.Pojo;

import lombok.Data;

import java.time.Instant;

@Data
public class PostDTO {
    private long id;
    private long userId;    //用户id
    private String postTitle;   //帖子标题
    private String postContent; //帖子内容
    private Instant updateTime; //更新时间
}
