package yin.xuebiblockchain.Service;

import yin.xuebiblockchain.Pojo.Result;

public interface UserService {

    Result getUserClass(Long id);

    Result getUserFans(Long id);

    Result getUserFollow(Long id);

    Result getUserIcon(Long id);

    Result getUserAddress(String publicKey);

    /** 创建区块链账户（同时保存 address 和 public_key） */
    Result createUserAccount(String address, String publicKey, Long id);

    Result createUserVIP(Long id);

    Long getUserId(String nickName);

    Result dailySignIn();

    Result updateNickName(Long userId, String nickName);
}