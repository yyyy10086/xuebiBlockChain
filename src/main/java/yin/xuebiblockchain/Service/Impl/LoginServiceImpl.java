package yin.xuebiblockchain.Service.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yin.xuebiblockchain.Mapper.LoginMapper;
import yin.xuebiblockchain.Pojo.LoginDTO;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.User;
import yin.xuebiblockchain.Service.LoginService;
import yin.xuebiblockchain.Utils.RegexUtils;

import java.util.UUID;

import static yin.xuebiblockchain.Utils.SystemConstants.INITIAL_BALANCE;
import static yin.xuebiblockchain.Utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * 登录服务实现类
 *
 * 【改造说明】
 * 1. createUserFromPhone 方法中设置初始积分为 100（INITIAL_BALANCE 常量）
 * 2. 新用户注册时，balance 字段在 LoginMapper.save() 的 SQL 中已设为 100
 * 3. 昵称前缀从 "user_" 改为 "campus_"
 *
 * 其余登录逻辑（验证码发送、Session存储、Token生成）保持不变。
 */
@Service
@Slf4j
public class LoginServiceImpl extends ServiceImpl<LoginMapper, User> implements LoginService {

    @Resource
    private LoginMapper loginMapper;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.error("手机号格式错误");
        }

        String code = RandomUtil.randomNumbers(6);
        session.setAttribute("code:" + phone, code);
        session.setMaxInactiveInterval(120);

        log.info("发送短信验证码成功，验证码: {}", code);
        return Result.success();
    }

    @Override
    public Result login(LoginDTO loginDTO, HttpSession session) {
        String phone = loginDTO.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.error("手机号格式错误");
        }

        String sessionCode = (String) session.getAttribute("code:" + phone);
        if (sessionCode == null || !sessionCode.equals(loginDTO.getCode())) {
            return Result.error("验证码错误");
        }

        User user = loginMapper.selectByPhone(phone);
        if (user == null) {
            user = createUserFromPhone(phone);
            log.info("新用户注册成功，手机号: {}, 初始积分: {}", phone, INITIAL_BALANCE);
        }

        String token = UUID.randomUUID().toString();
        session.setAttribute("token:" + token, user.getId());
        log.info("登录成功，用户ID: {}, token: {}", user.getId(), token);

        return Result.success(token);
    }

    /**
     * 根据手机号创建新用户
     *
     * 使用 loginMapper.save(user) 调用 XML 中定义的 save 方法。
     * 数据库 balance 字段的 DEFAULT 值是 100，
     * 所以即使 XML 中的 INSERT 没有包含 balance 字段，
     * 新用户也会自动获得 100 积分。
     */
    private User createUserFromPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        user.setBalance(INITIAL_BALANCE);

        // 设置默认密码 "123" 的 BCrypt 哈希
        String defaultPassword = "123";
        String hashedPassword = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);

        loginMapper.save(user);
        return user;
    }

    @Override
    public Result loginByPassword(LoginDTO loginDTO, HttpSession session) {
        String phone = loginDTO.getPhone();
        String password = loginDTO.getPassword();

        // 1. 校验手机号格式
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.error("手机号格式错误");
        }

        // 2. 查询用户
        User user = loginMapper.selectByPhone(phone);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 检查密码字段是否为空（兼容老用户）
        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.isEmpty()) {
            // 老用户没有密码，可以允许使用默认密码 "123" 登录并同时更新密码
            if ("123".equals(password)) {
                // 更新密码为加密后的 "123"
                String newHashedPassword = BCrypt.hashpw("123", BCrypt.gensalt());
                user.setPassword(newHashedPassword);
                loginMapper.updateById(user);
            } else {
                return Result.error("密码错误");
            }
        } else {
            // 4. 验证密码
            boolean passwordMatch = BCrypt.checkpw(password, storedPassword);
            if (!passwordMatch) {
                return Result.error("密码错误");
            }
        }

        // 5. 生成 token 存入 session
        String token = UUID.randomUUID().toString();
        session.setAttribute("token:" + token, user.getId());
        session.setMaxInactiveInterval(30 * 60); // 30分钟有效期

        log.info("密码登录成功，用户ID: {}, token: {}", user.getId(), token);
        return Result.success(token);
    }


}