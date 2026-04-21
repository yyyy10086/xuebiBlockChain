package yin.xuebiblockchain.Pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 资源数据传输对象
 *
 * 【新手解释】
 * 这个 DTO 用于前端展示资源信息。
 * 相比 Resource 实体类，它额外包含了分享者的昵称（ownerName），
 * 这样前端不需要再发一次请求去查用户信息。
 */
@Data
public class ResourceDTO {
    /** 资源ID */
    private Long id;

    /** 资源名称 */
    private String name;

    /** 资源描述 */
    private String description;

    /** 资源类型：DIGITAL / PHYSICAL */
    private String resourceType;

    /** 分类标签：教材、电子设备、学习笔记等 */
    private String category;

    /** 借用所需积分 */
    private Integer pointsCost;

    /** 状态：AVAILABLE / LENT / OFFLINE */
    private String status;

    /** 分享者ID */
    private Long ownerId;

    /** 分享者昵称（联表查询获得）*/
    private String ownerName;

    /** 分享者区块链地址 */
    private String ownerAddress;

    /** 资源图片URL */
    private String imageUrl;

    /** 借用次数 */
    private Integer borrowCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;
}