package yin.xuebiblockchain.Service.Impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import yin.xuebiblockchain.Mapper.SignInMapper;
import yin.xuebiblockchain.Mapper.UserMapper;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.UserDTO;
import yin.xuebiblockchain.Service.UserService;
import yin.xuebiblockchain.Utils.UserHolder;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private SignInMapper signInMapper;

    @Override
    public Result getUserClass(Long id) {
        Long level = userMapper.getUserClass(id);
        return Result.success(level != null ? level : 0);
    }

    @Override
    public Result getUserFans(Long id) {
        Long fans = userMapper.getUserFans(id);
        return Result.success(fans != null ? fans : 0);
    }

    @Override
    public Result getUserFollow(Long id) {
        Long follow = userMapper.getUserFollow(id);
        return Result.success(follow != null ? follow : 0);
    }

    @Override
    public Result getUserIcon(Long id) {
        String userIcon = userMapper.getUserIcon(id);
        return Result.success(userIcon != null && !userIcon.isEmpty() ? userIcon : "");
    }

    @Override
    public Result getUserAddress(String publicKey) {
        Long balance = userMapper.getUserAddress(publicKey);
        return Result.success(balance != null ? balance : 0);
    }

    /** 创建区块链账户（同时保存 address 和 public_key） */
    @Override
    public Result createUserAccount(String address, String publicKey, Long id) {
        // 检查用户是否已有公钥
        String existingPublicKey = userMapper.getPublicKeyById(id);
        if (existingPublicKey != null && !existingPublicKey.isEmpty()) {
            return Result.error("您已经创建过区块链账户，无需重复创建");
        }

        int rows = userMapper.createUserAccount(publicKey, publicKey, id);
        if (rows > 0) {
            return Result.success("账户创建成功，已绑定区块链地址");
        } else {
            return Result.error("账户创建失败，请重试");
        }
    }

    @Override
    public Result createUserVIP(Long id) {
        Long currentLevel = userMapper.getUserClass(id);
        if (currentLevel != null && currentLevel > 0) {
            return Result.success("用户已经是VIP会员");
        }
        int row = userMapper.createUserVIP(id);
        if (row > 0) {
            return Result.success("VIP会员创建成功");
        } else {
            return Result.error("VIP会员创建失败");
        }
    }

    @Override
    public Long getUserId(String nickName) {
        return userMapper.getUserId(nickName);
    }

    @Override
    public Result dailySignIn() {
        UserDTO user = UserHolder.getUser();
        if (user == null) return Result.error("请先登录");

        Date today = new Date();
        java.sql.Date sqlToday = new java.sql.Date(today.getTime());

        // 检查今日是否已签到
        int count = signInMapper.countToday(user.getId(), sqlToday);
        if (count > 0) {
            return Result.error("今日已签到，明天再来吧");
        }

        // 奖励积分（例如每天5分）
        int points = 5;
        // 连续签到额外奖励逻辑可自行扩展

        // 更新用户积分和最后签到日期
        int updated = userMapper.signIn(user.getId(), points);
        if (updated > 0) {
            // 插入签到记录
            signInMapper.insertRecord(user.getId(), sqlToday, points);
            // 刷新用户信息到 UserHolder（可选）
            user.setBalance(user.getBalance() + points);
            return Result.success("签到成功，获得 " + points + " 积分");
        }
        return Result.error("签到失败");
    }

    @Override
    public Result updateNickName(Long userId, String nickName) {
        if (nickName == null || nickName.trim().isEmpty()) {
            return Result.error("昵称不能为空");
        }
        int rows = userMapper.updateNickName(userId, nickName.trim());
        if (rows > 0) {
            return Result.success("昵称修改成功");
        }
        return Result.error("昵称修改失败");
    }
}