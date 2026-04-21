package yin.xuebiblockchain;

import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import yin.xuebiblockchain.Pojo.*;

import java.util.*;

import static yin.xuebiblockchain.Utils.SystemConstants.LEVEL_DB_STORE;

@SpringBootTest
class XuebiBlockChainApplicationTests {
     @Resource
     private Mempool mempool;


    @Test
    void contextLoads() {
        try {
            // 打开数据库
            LevelDBUtil.open(LEVEL_DB_STORE);
            // 执行相关操作
            // 创建创世区块

            Block genesisBlock = new Block();
            genesisBlock.setPreviousHash("0"); // 创世区块没有前置哈希
            genesisBlock.setTimestamp(System.currentTimeMillis());
            genesisBlock.setTransactions(new ArrayList<>()); // 创世区块没有交易
            genesisBlock.setDifficulty(0L);
            genesisBlock.setNumber(0L); // 创世区块的高度为 0
            genesisBlock.setGasLimit(0);
            genesisBlock.setGasUsed(0);
            genesisBlock.calculateMerkleRoot(); // 空交易列表的默克尔根可能是一个固定值（如空字符串）
            genesisBlock.mineBlock(); // 如果需要挖矿逻辑

            System.out.println("number:" + genesisBlock.getNumber());
            // 保存创世区块到数据库
            LevelDBUtil.saveBlock(genesisBlock);

            // 更新最新区块哈希
            LevelDBUtil.put("latest", genesisBlock.getHash());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭数据库
                LevelDBUtil.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    void getBlockFromDB() {
        try {
            // 打开数据库
//            LevelDBUtil.open(LEVEL_DB_STORE);

            // 获取最新区块哈希
            String latestBlockHash = LevelDBUtil.get("latest");
            if (latestBlockHash != null) {
                System.out.println("Latest Block Hash: " + latestBlockHash);

                // 根据哈希获取区块数据
                String blockData = LevelDBUtil.get(latestBlockHash);
                if (blockData != null) {
                    System.out.println("Block Data: " + blockData);

                    // 反序列化区块
                    Block block = new Gson().fromJson(blockData, Block.class);
                    System.out.println("Block Details: " + block.toString());
                } else {
                    System.out.println("No block found for hash: " + latestBlockHash);
                }
            } else {
                System.out.println("No latest block found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭数据库
                LevelDBUtil.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void getBlockStore() {
        //从内存池拿出交易
        List<Transaction> allTransactions = mempool.getAllTransactions();
        for (Transaction transaction : allTransactions) {
            System.out.println(transaction);

        }
    }

    @Test
    void getBlockByHash() throws Exception {
        try {
            Map<String, String> allData = LevelDBUtil.getAll();
            for (Map.Entry<String, String> entry : allData.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getBlockByNumber() throws Exception {
        LevelDBUtil.delete("f9a63bd8c60d8741715c08e19a8ab97d03438058069665dbbad36f3f9338fe9f");
        LevelDBUtil.delete("6a57e9b83de7b9a398287132bd4c37eef9e526b74f91a07f735aa983060f0288");
    }
}
