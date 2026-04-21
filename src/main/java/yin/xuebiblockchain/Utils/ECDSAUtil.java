package yin.xuebiblockchain.Utils;

import java.math.BigInteger;

/**
 * ECDSA 数字签名验证工具
 *
 * 【改造说明】
 * 1. buildMessage 方法增加了 resourceId 和 recordType 参数
 * 2. 保留原有签名验证逻辑不变（这是区块链安全的核心）
 * 3. 新增 buildResourceMessage 方法用于资源共享记录的签名
 *
 * 【新手解释】
 * ECDSA（椭圆曲线数字签名算法）是一种密码学签名技术：
 * - 用户用自己的私钥对消息进行签名
 * - 任何人都可以用用户的公钥验证签名是否有效
 * - 如果消息被篡改，签名验证会失败
 *
 * 在我们的平台中，每笔资源共享记录都需要借用者的签名，
 * 确保这笔记录确实是本人发起的，不可伪造。
 */
public class ECDSAUtil {

    /**
     * 验证 ECDSA 签名
     *
     * 【核心原理】
     * 1. 对消息做双重 SHA-256 哈希，得到消息摘要 z
     * 2. 解析签名的 r 和 s 分量
     * 3. 用公钥进行椭圆曲线运算
     * 4. 验证计算结果的 x 坐标是否等于 r
     *
     * 如果相等，说明签名有效——只有持有对应私钥的人才能生成这个签名
     */
    public static boolean verifySignature(String message, String rHex, String sHex, String publicKeyHex) throws Exception {
        try {
            // 1. 双重 SHA-256 哈希
            BigInteger z = ECCUtil.hashToBigInteger(message);

            // 2. 解析 r 和 s
            BigInteger r = new BigInteger(rHex, 16);
            BigInteger s = new BigInteger(sHex, 16);

            // 3. 校验 r 和 s 的范围（必须在 [1, N-1] 内）
            if (r.signum() <= 0 || r.compareTo(ECCUtil.N) >= 0 ||
                    s.signum() <= 0 || s.compareTo(ECCUtil.N) >= 0) {
                return false;
            }

            // 4. 解压公钥
            ECCUtil.ECPoint publicKey = ECCUtil.decompressPublicKey(publicKeyHex);

            // 5. 计算 s 的模逆
            BigInteger sInv = s.modInverse(ECCUtil.N);

            // 6. 计算 u1 和 u2
            BigInteger u1 = z.multiply(sInv).mod(ECCUtil.N);
            BigInteger u2 = r.multiply(sInv).mod(ECCUtil.N);

            // 7. 计算 u1 * G + u2 * PublicKey
            ECCUtil.ECPoint point = ECCUtil.pointAdd(
                    ECCUtil.pointMultiply(u1, ECCUtil.G),
                    ECCUtil.pointMultiply(u2, publicKey)
            );

            // 8. 验证签名：r 是否等于计算结果的 x 坐标 (mod N)
            return point.getX().mod(ECCUtil.N).equals(r);
        } catch (Exception e) {
            System.err.println("签名验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 构建原始交易消息字符串（兼容旧版本）
     *
     * 【重要】前端和后端必须用完全相同的格式构建消息，
     * 否则签名验证会失败！JSON 的 key 顺序、空格、引号都必须一致。
     */
    public static String buildMessage(String senderAddress, String recipientAddress,
                                      int amount, String timestamp) {
        return String.format(
                "{\"senderAddress\":\"%s\",\"recipientAddress\":\"%s\",\"amount\":%d,\"timestamp\":\"%s\"}",
                senderAddress, recipientAddress, amount, timestamp
        );
    }

    /**
     * 【新增】构建资源共享记录的消息字符串
     *
     * 增加了 resourceId 和 recordType 字段，
     * 这样签名不仅保护了积分信息，也保护了资源关联信息。
     *
     * 【前端必须同步修改】
     * 前端的 encryptUtil.ts 中也需要用完全相同的格式来构建消息！
     */
    public static String buildResourceMessage(String senderAddress, String recipientAddress,
                                              int amount, String timestamp, Long resourceId, String recordType) {
        // 格式必须与前端完全一致：无空格，字段顺序固定
        return String.format("{\"senderAddress\":\"%s\",\"recipientAddress\":\"%s\",\"amount\":%d,\"timestamp\":\"%s\",\"resourceId\":%d,\"recordType\":\"%s\"}",
                senderAddress, recipientAddress, amount, timestamp, resourceId, recordType);
    }

    /**
     * 辅助方法：字节数组转十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}