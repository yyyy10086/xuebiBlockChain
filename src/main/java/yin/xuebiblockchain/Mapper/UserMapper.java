package yin.xuebiblockchain.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.jdbc.SQL;
import yin.xuebiblockchain.Pojo.User;

import java.util.Date;

/**
 * 用户数据库操作接口
 *
 * 【改造说明】
 * 1. 继承 BaseMapper<User>，获得 MyBatis-Plus 的通用 CRUD
 * 2. getUserAddress 方法改为从 user 表直接查 balance（不再查独立的 account 表）
 * 3. createUserAccount 方法改为更新 user 表的 publicKey 字段
 * 4. 新增 initBalance 方法，注册时自动分配初始积分
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {



    Long getUserClass(@Param("id") Long id);

    Long getUserFans(@Param("id") Long id);

    Long getUserFollow(@Param("id") Long id);

    String getUserIcon(@Param("id") Long id);

    Long getUserAddress(@Param("publicKey") String publicKey);

    int createUserAccount(@Param("address") String address,
                          @Param("publicKey") String publicKey,
                          @Param("id") Long id);

    int createUserVIP(@Param("id") Long id);

    Long getUserId(@Param("nickName") String nickName);

    // ===== 新增方法（SQL 需要添加到 UserMapper.xml 中）=====

    /**
     * 初始化用户积分
     */
    int initBalance(@Param("id") Long id, @Param("balance") Long balance);

    /**
     * 通过用户ID查询公钥
     */
    String getPublicKeyById(@Param("id") Long id);

    /**
     * 通过公钥查询用户ID
     */
    Long getUserIdByPublicKey(@Param("publicKey") String publicKey);

    // 签到相关
    int signIn(@Param("userId") Long userId, @Param("points") int points);
    Date getLastSignDate(@Param("userId") Long userId);
    int addPointsById(@Param("userId") Long userId, @Param("amount") int amount);

    int updateNickName(@Param("userId") Long userId, @Param("nickName") String nickName);
}