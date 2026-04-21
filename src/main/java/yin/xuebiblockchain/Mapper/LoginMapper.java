package yin.xuebiblockchain.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import yin.xuebiblockchain.Pojo.User;

/**
 * 登录相关数据库操作
 * 所有 SQL 在 LoginMapper.xml 中定义，这里只有方法签名，没有任何 @Select/@Insert 注解
 */
@Mapper
public interface LoginMapper extends BaseMapper<User> {

    User selectByPhone(@Param("phone") String phone);

    Integer save( User user);
}