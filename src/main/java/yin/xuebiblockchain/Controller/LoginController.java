package yin.xuebiblockchain.Controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.LoginDTO;
import yin.xuebiblockchain.Pojo.User;
import yin.xuebiblockchain.Pojo.UserDTO;
import yin.xuebiblockchain.Service.LoginService;
import yin.xuebiblockchain.Utils.UserHolder;

@Slf4j
@RestController
@RequestMapping("/user")
public class LoginController {

    @Resource
    private LoginService loginService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        return loginService.login(loginDTO, session);
    }

    @PostMapping("/sendCode")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        return loginService.sendCode(phone, session);
    }

    @GetMapping("/me")
    public Result me() {
        UserDTO user = UserHolder.getUser();
        log.info("当前登录用户: {}", user);
        return Result.success(user);
    }

    @PostMapping("/loginByPassword")
    public Result loginByPassword(@RequestBody LoginDTO loginDTO, HttpSession session) {
        return loginService.loginByPassword(loginDTO, session);
    }
}