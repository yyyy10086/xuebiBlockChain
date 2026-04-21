package yin.xuebiblockchain.Utils;

import java.util.ArrayList;
import java.util.List;

public class MerkleTreeUtil {
    public static String getMerkleRoot(List<String> transactionHashes) {
        if (transactionHashes.isEmpty()) return "";
        while (transactionHashes.size() > 1) {
            List<String> newLevel = new ArrayList<>();
            for (int i = 0; i < transactionHashes.size(); i += 2) {
                String left = transactionHashes.get(i);
                String right = (i + 1 < transactionHashes.size()) ? transactionHashes.get(i + 1) : left;
                newLevel.add(HashUtil.applySha256(left + right));
            }
            transactionHashes = newLevel;
        }
        return transactionHashes.get(0);
    }
}
