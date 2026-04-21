package yin.xuebiblockchain.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

@Mapper
public interface FileUploadMapper {

    int save(@Param("userId") Long userId,@Param("url") String url);
}
