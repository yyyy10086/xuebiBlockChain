package yin.xuebiblockchain.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;

@Mapper
public interface SignInMapper {
    // 插入签到记录
    int insertRecord(@Param("userId") Long userId, @Param("signDate") Date signDate, @Param("points") int points);
    // 检查今日是否已签到
    int countToday(@Param("userId") Long userId, @Param("signDate") Date signDate);
}
