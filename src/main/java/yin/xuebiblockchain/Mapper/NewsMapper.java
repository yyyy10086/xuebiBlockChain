package yin.xuebiblockchain.Mapper;

import org.apache.ibatis.annotations.Mapper;
import yin.xuebiblockchain.Pojo.NewsDTO;

import java.util.List;

@Mapper
public interface NewsMapper {
    List<NewsDTO> getNews();
}
