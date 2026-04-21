package yin.xuebiblockchain.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    //单次sha-256
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //双重sha-256
    public static String applyDoubleSHA256(String input) {
        try {
            // 获取 SHA-256 MessageDigest 实例
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 第一次 SHA-256
            byte[] firstHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // 第二次 SHA-256
            byte[] secondHash = digest.digest(firstHash);

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : secondHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found!", e);
        }
    }

}
