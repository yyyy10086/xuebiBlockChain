package yin.xuebiblockchain.Utils;

import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;

public class AddressUtil {
    private static final ConcurrentHashMap<String, PublicKey> addressToPublicKeyMap = new ConcurrentHashMap<>();

    // 添加地址和公钥映射
    public static void addAddress(String address, PublicKey publicKey) {
        addressToPublicKeyMap.put(address, publicKey);
    }

    // 获取地址对应的公钥
    public static PublicKey getPublicKeyFromAddress(String address) {
        return addressToPublicKeyMap.get(address);
    }
}
