package yin.xuebiblockchain.Service;


import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.Transaction;

public interface TransactionService {

    Result getTransactionMessage(Transaction transaction);
    /**
     * 查询交易历史记录
     * @param address 公钥地址
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 交易记录列表
     */
    Result getTransactionHistory(String address, int page, int size);


}
