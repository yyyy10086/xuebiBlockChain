package yin.xuebiblockchain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("yin.xuebiblockchain.Mapper")
@SpringBootApplication
public class XuebiBlockChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuebiBlockChainApplication.class, args);
    }

}
