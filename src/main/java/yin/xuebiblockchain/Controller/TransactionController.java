package yin.xuebiblockchain.Controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.Transaction;
import yin.xuebiblockchain.Pojo.UserDTO;
import yin.xuebiblockchain.Service.TransactionService;
import yin.xuebiblockchain.Utils.UserHolder;


@RestController
@Slf4j
@RequestMapping("/transaction")
public class TransactionController {
    @Resource
    private TransactionService transactionService;

    @PostMapping("/sendAmount")
    public Result getTransactionMessage(@RequestBody Transaction transaction) {
        return transactionService.getTransactionMessage(transaction);

    }

    @GetMapping("/history")
    public Result getTransactionHistory(@RequestParam(required = false) String address,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        if (address == null || address.isEmpty()) {
            UserDTO user = UserHolder.getUser();
            if (user == null) return Result.error("用户未登录");
            address = user.getPublicKey();
            if (address == null) return Result.error("用户尚未创建区块链账户");
        }
        return transactionService.getTransactionHistory(address, page, size);
    }

}
