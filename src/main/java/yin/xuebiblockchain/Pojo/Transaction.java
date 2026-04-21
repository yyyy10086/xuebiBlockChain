package yin.xuebiblockchain.Pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import yin.xuebiblockchain.Utils.HashUtil;

import java.sql.Timestamp;

/**
 * 资源共享记录（交易记录）
 *
 * 【改造说明】
 * 原来这个类表示"加密货币转账"，现在改造为"校园资源共享记录"。
 *
 * 核心变化：
 * 1. 新增 resourceId、resourceName、resourceType —— 关联具体的共享资源
 * 2. 新增 recordType —— 区分"借用申请"、"确认收到"、"归还"三种操作
 * 3. amount 的含义从"转账金额"变为"消耗/获得的积分数"
 * 4. senderAddress 变为"借用者地址"，recipientAddress 变为"分享者地址"
 * 5. 保留 ECDSA 签名验证，确保每笔记录不可伪造
 *
 * 【区块链角色】
 * 这个类的实例会被打包进 Block 的 transactions 列表，
 * 通过默克尔树计算根哈希，确保数据不可篡改。
 */
@Data
public class Transaction {

    // ============ 原有字段（含义调整）============

    /**
     * 借用者的公钥地址
     * 原来叫"发送方地址"，现在是发起借用的学生的区块链地址
     */
    private String senderAddress;

    /**
     * 分享者的公钥地址
     * 原来叫"接收方地址"，现在是资源拥有者的区块链地址
     */
    private String recipientAddress;

    /**
     * 积分数量
     * 原来是"转账金额"，现在是本次资源共享涉及的积分数
     * 借用时：借用者扣除这么多积分
     * 确认时：分享者获得这么多积分
     */
    private Integer amount;

    /**
     * 记录的时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Timestamp timestamp;

    /**
     * ECDSA 数字签名（r, s 两个分量）
     * 【保留原因】论文中体现区块链的安全性——每笔记录都有密码学签名
     */
    private Signature signature;

    /**
     * 签名者的公钥（十六进制字符串）
     * 用于验证签名是否由该用户本人发起
     */
    private String publicKey;

    /**
     * Gas 消耗量（保留区块链概念，用于控制区块容量）
     */
    private long gasUsed;

    // ============ 新增字段（资源共享相关）============

    /**
     * 关联的资源ID
     * 对应 Resource 表的主键，标明这笔记录涉及哪个共享资源
     */
    private Long resourceId;

    /**
     * 资源名称（冗余存储，方便区块链记录直接展示）
     *
     * 【为什么冗余？】
     * 区块链上的记录应该是自包含的，不需要再查数据库就能看到完整信息。
     * 这和以太坊交易中包含备注信息是同样的设计理念。
     */
    private String resourceName;

    /**
     * 资源类型：DIGITAL 或 PHYSICAL
     */
    private String resourceType;

    /**
     * 记录类型，标明这笔交易的具体操作：
     * - BORROW_REQUEST: 借用申请（借用者发起）
     * - CONFIRM_RECEIVED: 确认收到（借用者确认，此时积分转移）
     * - RETURN: 归还资源（借用者归还实体物品）
     *
     * 【设计说明】
     * 通过不同的 recordType，同一个 Transaction 类可以表达完整的资源流转生命周期，
     * 所有操作都上链，实现完整的溯源追踪。
     */
    private String recordType;

    // ============ 记录类型常量 ============

    /** 借用申请 */
    public static final String TYPE_BORROW_REQUEST = "BORROW_REQUEST";
    /** 确认收到（积分转移在此时发生）*/
    public static final String TYPE_CONFIRM_RECEIVED = "CONFIRM_RECEIVED";
    /** 归还资源 */
    public static final String TYPE_RETURN = "RETURN";

    // ============ 内部类：数字签名 ============

    /**
     * ECDSA 签名的两个分量
     * r 和 s 都是64字符的十六进制字符串
     */
    @Data
    public static class Signature {
        private String r;
        private String s;
    }

    // ============ 构造方法 ============

    public Transaction() {
    }

    /**
     * 完整构造方法（包含资源信息）
     */
    public Transaction(String senderAddress, String recipientAddress, Integer amount,
                       Timestamp timestamp, Signature signature, String publicKey,
                       long gasUsed, Long resourceId, String resourceName,
                       String resourceType, String recordType) {
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.timestamp = timestamp;
        this.signature = signature;
        this.publicKey = publicKey;
        this.gasUsed = gasUsed;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.recordType = recordType;
    }

    /**
     * 兼容旧版本的构造方法（不含资源信息）
     * 保留是为了不破坏 Block.java 中已有的代码
     */
    public Transaction(String senderAddress, String recipientAddress, Integer amount,
                       Timestamp timestamp, Signature signature, String publicKey, long gasUsed) {
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.timestamp = timestamp;
        this.signature = signature;
        this.publicKey = publicKey;
        this.gasUsed = gasUsed;
    }

    // ============ 核心方法 ============

    /**
     * 构建签名消息
     *
     * 【改造说明】
     * 增加了 resourceId 和 recordType 到消息中，
     * 这样签名不仅保护了积分转移信息，也保护了资源关联信息。
     * 前端签名时也需要用完全相同的格式构建消息！
     */
    public String getMessage() {
        return String.format(
                "{\"senderAddress\":\"%s\",\"recipientAddress\":\"%s\",\"amount\":%d,\"timestamp\":\"%s\",\"resourceId\":%s,\"recordType\":\"%s\"}",
                senderAddress, recipientAddress, amount, timestamp,
                resourceId != null ? resourceId.toString() : "null",
                recordType != null ? recordType : ""
        );
    }

    /**
     * 计算交易哈希
     *
     * 【区块链作用】
     * 这个哈希用于：
     * 1. 内存池中标识唯一交易
     * 2. 默克尔树的叶子节点
     * 3. 防止交易被篡改
     */
    public String calculateHash() {
        String data = senderAddress + recipientAddress + amount + timestamp
                + resourceId + resourceName + recordType + signature;
        return HashUtil.applySha256(data);
    }

    // ============ 手动 Getter/Setter（保持与原代码兼容）============
    // 注意：@Data 注解已经自动生成了这些方法，
    // 但原代码中 getAmount() 返回 double，这里保持一致避免Bug

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    /**
     * 【注意】原代码返回 double，但实际类型是 Integer
     * 这里统一返回 double 以兼容 TransactionServiceImpl 中的强转
     */
    public double getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "sender=" + senderAddress +
                ", recipient=" + recipientAddress +
                ", amount=" + amount +
                ", resourceId=" + resourceId +
                ", resourceName=" + resourceName +
                ", recordType=" + recordType +
                '}';
    }
}