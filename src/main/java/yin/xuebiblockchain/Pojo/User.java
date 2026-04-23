package yin.xuebiblockchain.Pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * 【改造说明】
 * 原来用户的积分（余额）存储在单独的 account 表里，通过 publicKey 关联。
 * 现在把 balance 和 publicKey 直接放进 user 表，简化查询逻辑。
 *
 * 新增字段：
 * 1. publicKey — 用户的区块链公钥地址（创建账户时生成）
 * 2. balance — 信用积分余额（新用户默认100分）
 * 3. studentId — 学号（校园平台特有）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号码（用于登录验证码）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Integer role; // 0-普通用户，1-管理员

    /**
     * 密码（加密存储，目前系统用验证码登录，此字段备用）
     */
    private String password;

    /**
     * 昵称（默认是 "user_" + 随机字符串）
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称不能超过20个字符")
    private String nickName;

    /**
     * 用户头像URL
     */
    private String icon = "";

    /**
     * 【新增】学号 — 校园平台中用于标识学生身份
     * 可选字段，用户可以在个人中心补充填写
     */
    private String studentId;

    /**
     * 【新增】区块链公钥地址
     * 用户创建账户时通过 ECC 算法生成，用于：
     * 1. 标识用户在区块链上的身份
     * 2. 验证 ECDSA 签名
     * 3. 记录积分转移的发送方/接收方
     */
    private String publicKey;

    /**
     * 【新增】信用积分余额
     * - 新用户注册时自动获得 100 积分
     * - 借用资源时扣除积分
     * - 分享资源被确认收到时获得积分
     * - 所有积分变动都记录在区块链上
     */
    private Long balance;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}