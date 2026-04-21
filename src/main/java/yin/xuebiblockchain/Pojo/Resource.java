package yin.xuebiblockchain.Pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 校园共享资源实体类
 *
 * 【新手解释】
 * 这个类代表平台上的一个共享资源，比如一本教材、一台相机、一份课程笔记等。
 * 每个资源都有一个唯一的ID，归属于某个用户（分享者）。
 *
 * @TableName 告诉 MyBatis-Plus 这个类对应数据库的 "resource" 表
 * @Data 是 Lombok 注解，自动生成 getter/setter/toString 等方法
 */
@Data
@TableName("resource")
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资源名称，例如："高等数学教材第七版"、"佳能EOS R5相机"
     */
    private String name;

    /**
     * 资源描述，详细说明资源的状态、规格等
     */
    private String description;

    /**
     * 资源类型：DIGITAL（电子文档）或 PHYSICAL（实体物品）
     *
     * 【设计说明】
     * 用字符串而不是枚举，是为了和数据库VARCHAR字段兼容，减少类型转换Bug
     */
    private String resourceType;

    /**
     * 资源分类标签，例如："教材"、"电子设备"、"学习笔记"、"体育器材"
     */
    private String category;

    /**
     * 借用所需积分（由分享者定价）
     */
    private Integer pointsCost;

    /**
     * 资源当前状态：AVAILABLE（可借用）、LENT（已借出）、OFFLINE（已下架）
     */
    private String status;

    /**
     * 分享者的用户ID（外键，关联 user 表）
     */
    private Long ownerId;

    /**
     * 分享者的公钥地址（用于区块链记录）
     */
    private String ownerAddress;

    /**
     * 资源图片URL（可选，存储在阿里云OSS或本地）
     */
    private String imageUrl;

    /**
     * 当前借用者的用户ID（如果已借出）
     */
    private Long borrowerId;

    /**
     * 借用次数（热门度指标）
     */
    private Integer borrowCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp borrowEndTime;

    // ============ 常量定义 ============

    /** 资源类型：电子文档 */
    public static final String TYPE_DIGITAL = "DIGITAL";
    /** 资源类型：实体物品 */
    public static final String TYPE_PHYSICAL = "PHYSICAL";

    /** 状态：可借用 */
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    /** 状态：已借出 */
    public static final String STATUS_LENT = "LENT";
    /** 状态：已下架 */
    public static final String STATUS_OFFLINE = "OFFLINE";

    public static final String STATUS_PENDING = "PENDING";
}