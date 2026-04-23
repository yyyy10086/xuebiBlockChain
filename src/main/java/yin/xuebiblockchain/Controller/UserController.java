package yin.xuebiblockchain.Controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.UserDTO;
import yin.xuebiblockchain.Service.UserService;
import yin.xuebiblockchain.Utils.UserHolder;

/**
 * 用户信息控制器
 *
 * 【改造说明】
 * 1. me() 方法不再返回硬编码的测试数据，改为返回 UserHolder 中的真实用户信息
 * 2. getUserFans / getUserFollow / getUserClass 方法从 user 表查询真实数据
 * 3. 新增 getUserBalance 接口，前端可以直接查询当前用户的积分余额
 * 4. 新增 createUserAccount 和 createUserVIP 接口（从原来的位置迁移过来）
 *
 * 【Bug修复】
 * 原来 me() 方法在 UserHolder.getUser() 为 null 时返回 id=1 的模拟用户，
 * 这在生产环境会导致所有未登录用户看到同一个假账户。现在改为返回错误提示。
 */
@Slf4j
@RestController
@RequestMapping("/userInfo")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 获取当前登录用户信息
     *
     * 【前端调用时机】
     * 前端 UserStore.ts 的 fetchUserInfo() 方法会调用这个接口，
     * 获取昵称、头像等信息并存入 Pinia store。
     */
    @GetMapping("/me")
    public Result me() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        return Result.success(user);
    }

    /**
     * 获取用户粉丝数
     */
    @GetMapping("/userFans")
    public Result getUserFans() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.success(0);
        }
        return userService.getUserFans(user.getId());
    }

    /**
     * 获取用户关注数
     */
    @GetMapping("/userFollow")
    public Result getUserFollow() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.success(0);
        }
        return userService.getUserFollow(user.getId());
    }

    /**
     * 获取用户VIP等级
     */
    @GetMapping("/userVIP")
    public Result getUserClass() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.success(0);
        }
        return userService.getUserClass(user.getId());
    }

    /**
     * 通过公钥查询用户积分余额
     *
     * 【前端调用】
     * UserStore.ts 的 fetchBalance() 方法调用，
     * 传入用户的公钥地址，返回积分余额。
     */
    @GetMapping("/userAddress")
    public Result getUserBalance(@RequestParam("public_key") String publicKey) {
        return userService.getUserAddress(publicKey);
    }

    /**
     * 创建用户区块链账户（绑定公钥）
     *
     * 【调用时机】
     * 用户在"创建账户"页面生成密钥对后，调用此接口将公钥存入数据库。
     */
    @PostMapping("/userAccount")
    public Result createUserAccount(@RequestParam("public_key") String publicKey) {  // 去掉 address 参数
        UserDTO user = UserHolder.getUser();
        if (user == null) return Result.error("用户未登录");
        // address 参数已废弃，直接传 publicKey 即可
        return userService.createUserAccount(publicKey, publicKey, user.getId());  // address = publicKey
    }

    /**
     * 创建用户VIP
     */
    @PostMapping("/createUserVIP")
    public Result createUserVIP() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        return userService.createUserVIP(user.getId());
    }

    @PostMapping("/signIn")
    public Result signIn() {
        return userService.dailySignIn();
    }

    @PutMapping("/nickName")
    public Result updateNickName(@RequestParam String nickName) {
        UserDTO user = UserHolder.getUser();
        if (user == null) return Result.error("请先登录");
        userService.updateNickName(user.getId(), nickName);
        return Result.success();
    }

    @PutMapping("/password")
    public Result updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        UserDTO user = UserHolder.getUser();
        if (user == null) return Result.error("请先登录");
        return userService.updatePassword(user.getId(), oldPassword, newPassword);
    }
}