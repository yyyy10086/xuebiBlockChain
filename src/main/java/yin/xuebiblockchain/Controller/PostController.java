package yin.xuebiblockchain.Controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Service.PostService;

@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {
    @Resource
    private PostService postService;

    @GetMapping
    public Result getPost(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return postService.getPostList(page, size);
    }

    @PostMapping("/sendPost")
    public Result sendPost(@RequestParam("postContent") String postContent) {
        return postService.sendPost(postContent);
    }

    @PostMapping("/like")
    public Result isLike(@RequestParam("postId") long postId) {
        return postService.isLike(postId);
    }

    @PostMapping("/views")
    public Result isView(@RequestParam("postId") long postId) {
        return postService.isView(postId);
    }

    @PostMapping("/share")
    public Result isShare(@RequestParam("postId") long postId) {
        return postService.isShare(postId);
    }
}