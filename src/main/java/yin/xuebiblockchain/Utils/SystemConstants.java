package yin.xuebiblockchain.Utils;

/**
 * 系统常量类
 *
 * 【改造说明】
 * 1. 新增校园资源共享平台专用常量
 * 2. 保留区块链相关常量（GAS_LIMIT、LEVEL_DB_STORE）
 * 3. 去掉与加密货币相关的概念
 */
public class SystemConstants {

    // ============ 用户相关 ============

    /** 用户昵称默认前缀 */
    public static final String USER_NICK_NAME_PREFIX = "campus_";

    /** 用户默认头像URL */
    public static final String USER_DEFAULT_ICON =
            "https://user-image10086.oss-cn-guangzhou.aliyuncs.com/avatar/%E7%94%A8%E6%88%B7%E9%BB%98%E8%AE%A4%E5%9B%BE%E7%89%87.jpg";

    /** 新用户注册时获得的初始积分 */
    public static final long INITIAL_BALANCE = 100L;

    // ============ 区块链相关 ============

    /** LevelDB 数据存储路径标识 */
    public static final String LEVEL_DB_STORE = "CAMPUS_LEVELDB";



    // ============ 资源相关 ============

    /** 资源默认图片 */
    public static final String RESOURCE_DEFAULT_IMAGE =
            "https://user-image10086.oss-cn-guangzhou.aliyuncs.com/resource/default.png";

    /** 资源类型枚举值 */
    public static final String RESOURCE_TYPE_DIGITAL = "DIGITAL";
    public static final String RESOURCE_TYPE_PHYSICAL = "PHYSICAL";

    /** 资源分类 */
    public static final String CATEGORY_TEXTBOOK = "教材";
    public static final String CATEGORY_NOTES = "学习笔记";
    public static final String CATEGORY_DEVICE = "电子设备";
    public static final String CATEGORY_SPORTS = "体育器材";
    public static final String CATEGORY_DAILY = "日常用品";
    public static final String CATEGORY_OTHER = "其他";
}