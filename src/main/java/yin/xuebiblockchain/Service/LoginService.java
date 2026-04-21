package yin.xuebiblockchain.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpSession;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.User;
import yin.xuebiblockchain.Pojo.LoginDTO;

public interface LoginService extends IService<User> {


    Result sendCode(String phone, HttpSession session);

    Result login(LoginDTO loginDTO, HttpSession session);

    Result loginByPassword(LoginDTO loginDTO, HttpSession session);

}