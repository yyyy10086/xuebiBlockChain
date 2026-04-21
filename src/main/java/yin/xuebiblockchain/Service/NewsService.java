package yin.xuebiblockchain.Service;

import org.springframework.stereotype.Service;
import yin.xuebiblockchain.Pojo.NewsDTO;
import yin.xuebiblockchain.Pojo.Result;


public interface NewsService {
    Result getNews();
}
