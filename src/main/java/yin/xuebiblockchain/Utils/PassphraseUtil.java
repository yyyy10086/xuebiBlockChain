package yin.xuebiblockchain.Utils;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

public class PassphraseUtil {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    // 单词库加载（BIP-39 标准）
    private static List<String> loadWordList() throws Exception {
        // 确保文件路径正确
        Path path = Path.of("src/main/java/yin/xuebiblockchain/english.txt"); // 替换为实际文件路径
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("单词库文件未找到：" + path);
        }

        List<String> wordList = Files.readAllLines(path);

        if (wordList.isEmpty()) {
            throw new IllegalArgumentException("单词库文件为空：" + path);
        }

        return wordList;
    }

    // 随机生成助记词
    public static List<String> generateMnemonic(int wordCount) throws Exception {
        List<String> wordList = loadWordList(); // 加载单词库
        List<String> mnemonic = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < wordCount; i++) {
            int index = random.nextInt(wordList.size()); // 确保单词库非空
            mnemonic.add(wordList.get(index));
        }

        return mnemonic;
    }

    public static List<String> getWords() {
        List<String> words = new ArrayList<>();
        try {
            // 生成 12 个助记词

            List<String> mnemonic = generateMnemonic(12);

            for (String word : mnemonic) {
                words.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("助记词生成失败：" + e.getMessage());
        }
        return words;
    }

}
