package yin.xuebiblockchain.Pojo;

import lombok.Data;

/**
 * 用户数据传输对象（DTO）
 *
 * 【新手解释】
 * DTO 是 Data Transfer Object 的缩写，用于在前端和后端之间传输数据。
 * 它和 User 实体类的区别是：DTO 只包含前端需要的字段，
 * 不会暴露敏感信息（比如密码、手机号）。
 *
 * 【改造说明】
 * 新增 balance（信用积分）和 publicKey（区块链地址）字段，
 * 这样前端可以直接展示用户的积分和地址。
 */
@Data
public class UserDTO {
    /** 用户昵称 */
    private String nickName;

    /** 用户ID */
    private Long id;

    /** 用户头像URL */
    private String icon;

    /** 【新增】信用积分余额 */
    private Long balance;

    /** 【新增】区块链公钥地址 */
    private String publicKey;

    /** 【新增】学号 */
    private String studentId;
    /** 区块链地址（此处与 publicKey 完全一致，统一使用 hex 公钥作为标识） */
    private String address;

    // getter/setter（lombok @Data 已自动生成，也可手动加）
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}