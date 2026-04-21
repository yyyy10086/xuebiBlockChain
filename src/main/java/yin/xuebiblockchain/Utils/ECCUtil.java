package yin.xuebiblockchain.Utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Objects;


public class ECCUtil {
    // 椭圆曲线参数 secp256k1
    public static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
    public static final BigInteger N = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
    public static final BigInteger A = BigInteger.ZERO;
    public static final BigInteger B = BigInteger.valueOf(7);


    public static class ECPoint {
        public static final ECPoint INFINITY = new ECPoint(null, null); // 无穷远点
        private final BigInteger x;
        private final BigInteger y;

        public ECPoint(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        public BigInteger getX() {
            return x;
        }

        public BigInteger getY() {
            return y;
        }

        public boolean isInfinity() {
            return x == null || y == null;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ECPoint point = (ECPoint) obj;
            return Objects.equals(x, point.x) && Objects.equals(y, point.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            if (this.isInfinity()) {
                return "Infinity";
            }
            return String.format("ECPoint(x=%s, y=%s)", x.toString(16), y.toString(16));
        }
    }

    // 基点 G
    public static final ECPoint G = new ECPoint(
            new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
            new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
    );

    public static ECPoint pointAdd(ECPoint p1, ECPoint p2) {
        if (p1.isInfinity()) return p2;
        if (p2.isInfinity()) return p1;
        if (p1.getX().equals(p2.getX()) && !p1.getY().equals(p2.getY())) return ECPoint.INFINITY;

        BigInteger lambda;
        if (p1.equals(p2)) {
            lambda = p1.getX().pow(2).multiply(BigInteger.valueOf(3)).add(A)
                    .multiply(p1.getY().multiply(BigInteger.TWO).modInverse(P)).mod(P);
        } else {
            lambda = p2.getY().subtract(p1.getY())
                    .multiply(p2.getX().subtract(p1.getX()).modInverse(P)).mod(P);
        }

        BigInteger x3 = lambda.pow(2).subtract(p1.getX()).subtract(p2.getX()).mod(P);
        BigInteger y3 = lambda.multiply(p1.getX().subtract(x3)).subtract(p1.getY()).mod(P);

        return new ECPoint(x3, y3);
    }

    public static ECPoint pointMultiply(BigInteger k, ECPoint point) {
        ECPoint result = ECPoint.INFINITY;
        ECPoint addend = point;

        while (k.signum() > 0) {
            if (k.testBit(0)) {
                result = pointAdd(result, addend);
            }
            addend = pointAdd(addend, addend);
            k = k.shiftRight(1);
        }
        return result;
    }


    public static byte[] doubleSHA256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] firstHash = digest.digest(input.getBytes("UTF-8"));
        return digest.digest(firstHash);
    }

    public static BigInteger hashToBigInteger(String input) throws Exception {
        byte[] hash = doubleSHA256(input);
        return new BigInteger(1, hash);
    }

    public static boolean verifySignature(String message, String publicKeyHex, String rHex, String sHex) throws Exception {
        // 双重 SHA-256
        BigInteger z = hashToBigInteger(message);

        // 签名参数 r, s
        BigInteger r = new BigInteger(rHex, 16);
        BigInteger s = new BigInteger(sHex, 16);

        if (r.signum() <= 0 || r.compareTo(N) >= 0 || s.signum() <= 0 || s.compareTo(N) >= 0) {
            return false; // r 和 s 必须在合法范围内
        }

        // 计算 s^-1 (mod N)
        BigInteger sInv = s.modInverse(N);

        // 计算 u1 和 u2
        BigInteger u1 = z.multiply(sInv).mod(N);
        BigInteger u2 = r.multiply(sInv).mod(N);

        // 恢复公钥
        ECPoint publicKey = decompressPublicKey(publicKeyHex);

        // 计算 u1 * G + u2 * PublicKey
        ECPoint point = pointAdd(
                pointMultiply(u1, G),
                pointMultiply(u2, publicKey)
        );

        // 签名验证： r == point.x (mod N)
        return point.getX().mod(N).equals(r);
    }

    public static ECPoint decompressPublicKey(String compressedKey) {
        BigInteger x = new BigInteger(compressedKey.substring(2), 16);
        BigInteger ySquare = x.pow(3).add(B).mod(P);
        BigInteger y = ySquare.modPow(P.add(BigInteger.ONE).shiftRight(2), P);

        if (!compressedKey.startsWith("02") && !compressedKey.startsWith("03")) {
            throw new IllegalArgumentException("无效的压缩公钥格式");
        }

        if ((y.testBit(0) && compressedKey.startsWith("02")) || (!y.testBit(0) && compressedKey.startsWith("03"))) {
            y = P.subtract(y);
        }
        return new ECPoint(x, y);
    }
}
