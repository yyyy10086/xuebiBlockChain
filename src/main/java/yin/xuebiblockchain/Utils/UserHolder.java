package yin.xuebiblockchain.Utils;

import yin.xuebiblockchain.Pojo.User;
import yin.xuebiblockchain.Pojo.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        tl.set(user); // 直接设置 user 到 ThreadLocal
    }

    public static UserDTO getUser() {
         return tl.get();

    }

    public static void removeUser() {
        tl.remove();
    }

    private static User convertToUser(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setNickName(userDTO.getNickName());
        user.setIcon(userDTO.getIcon());
        // 复制其他属性...
        return user;
    }
}
