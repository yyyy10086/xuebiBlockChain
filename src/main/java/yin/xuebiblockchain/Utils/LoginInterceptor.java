package yin.xuebiblockchain.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yin.xuebiblockchain.Mapper.LoginMapper;
import yin.xuebiblockchain.Mapper.UserMapper;
import yin.xuebiblockchain.Pojo.User;
import yin.xuebiblockchain.Pojo.UserDTO;

import jakarta.annotation.Resource;

/**
 * 登录拦截器
 *
 * 【改造说明】
 * 在创建 UserDTO 时，新增了 balance、publicKey、studentId 字段的赋值。
 * 这样后续 Controller 通过 UserHolder.getUser() 获取到的 UserDTO
 * 就包含完整的用户信息，包括积分余额和区块链地址。
 *
 * 【Bug修复】
 * 原来的 token 解析没有去掉 "Bearer " 前缀。
 * 前端 request.ts 中设置的 Authorization 头格式是 "Bearer xxx"，
 * 所以这里需要去掉前缀才能正确匹配 session 中的 token。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        // 【Bug修复】去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(401);
            return false;
        }

        Long userId = (Long) session.getAttribute("token:" + token);
        if (userId == null) {
            response.setStatus(401);
            return false;
        }

        // 从 MySQL 查询用户信息
        User user = userMapper.selectById(userId);       // ← 改成这行

        if (user == null) {
            response.setStatus(401);
            return false;
        }

        // 存入 ThreadLocal（包含 balance、publicKey）
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setNickName(user.getNickName());
        userDTO.setIcon(user.getIcon());
        userDTO.setBalance(user.getBalance());           // 关键：这里必须有值
        userDTO.setPublicKey(user.getPublicKey());
        userDTO.setStudentId(user.getStudentId());
        UserHolder.saveUser(userDTO);
        userDTO.setAddress(user.getPublicKey());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}