package yin.xuebiblockchain.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 积分与交易记录数据库操作
 *
 * 【重要】所有 SQL 定义在 TransactionMapper.xml 中，这里只声明方法签名。
 *
 * 【改造说明】
 * 保留原有的 sendAmount/addAmount 方法（XML 中已有 SQL）。
 * 新增 deductPoints、addPoints、getBalance 等方法（SQL 需要添加到 XML 中）。
 */
@Mapper
public interface TransactionMapper {

    // ===== 原有方法（SQL 已在 TransactionMapper.xml 中）=====

    boolean sendAmount(@Param("public_key") String senderAddress, @Param("amount") int amount);

    boolean addAmount(@Param("public_key") String recipientAddress, @Param("amount") int amount);

    // ===== 新增方法（SQL 需要添加到 TransactionMapper.xml 中）=====

    /**
     * 扣除积分（带余额检查，balance >= amount 才扣）
     */
    int deductPoints(@Param("publicKey") String publicKey, @Param("amount") int amount);

    /**
     * 增加积分
     */
    int addPoints(@Param("publicKey") String publicKey, @Param("amount") int amount);

    /**
     * 查询用户积分余额
     */
    Long getBalance(@Param("publicKey") String publicKey);

    /**
     * 保存交易记录到 MySQL
     */
    int saveTransactionRecord(@Param("senderAddress") String senderAddress,
                              @Param("recipientAddress") String recipientAddress,
                              @Param("amount") int amount,
                              @Param("timestamp") String timestamp,
                              @Param("resourceId") Long resourceId,
                              @Param("resourceName") String resourceName,
                              @Param("resourceType") String resourceType,
                              @Param("recordType") String recordType,
                              @Param("txHash") String txHash,
                              @Param("blockNumber") Long blockNumber);

    /**
     * 查询某个用户的交易记录
     */
    List<Map<String, Object>> findByAddress(@Param("address") String address,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    /**
     * 查询某个资源的流转历史（溯源功能）
     */
    List<Map<String, Object>> findByResourceId(@Param("resourceId") Long resourceId);

    void updateBlockNumber(@Param("txHash") String txHash, @Param("blockNumber") Long blockNumber);

}