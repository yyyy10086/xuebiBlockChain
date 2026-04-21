package yin.xuebiblockchain.Controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yin.xuebiblockchain.Pojo.NewsDTO;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Service.NewsService;

@RestController
@Slf4j
//@RequestMapping("/news")
public class NewsController {
    @Resource
    private NewsService newsService;

    @GetMapping("/news")
    //获取新闻
    public Result getNews(){
        return newsService.getNews();
    }
}
