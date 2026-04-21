package yin.xuebiblockchain.Service.Impl;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yin.xuebiblockchain.Mapper.NewsMapper;
import yin.xuebiblockchain.Pojo.NewsDTO;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Service.NewsService;

import java.util.List;
@Service
public class NewsServiceImpl implements NewsService {
    @Resource
    private NewsMapper newsMapper;

    @Override
    public Result getNews() {
        List<NewsDTO> newsDTOS = newsMapper.getNews();
        return Result.success(newsDTOS.toArray(new NewsDTO[0]));
    }
}
