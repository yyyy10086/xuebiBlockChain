package yin.xuebiblockchain.Service.Impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yin.xuebiblockchain.Mapper.ResourceMapper;
import yin.xuebiblockchain.Mapper.TransactionMapper;
import yin.xuebiblockchain.Mapper.UserMapper;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Pojo.Transaction;
import yin.xuebiblockchain.Service.FabricCliService;
import yin.xuebiblockchain.Service.TransactionService;
import yin.xuebiblockchain.Utils.ECDSAUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Resource
    private TransactionMapper transactionMapper;

    @Resource
    private ResourceMapper resourceMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FabricCliService fabricCliService;

    private static final String RES_STATUS_AVAILABLE = yin.xuebiblockchain.Pojo.Resource.STATUS_AVAILABLE;
    private static final String RES_STATUS_PENDING  = yin.xuebiblockchain.Pojo.Resource.STATUS_PENDING;
    private static final String RES_STATUS_LENT     = yin.xuebiblockchain.Pojo.Resource.STATUS_LENT;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result getTransactionMessage(Transaction transaction) {
        String identifier = transaction.getSenderAddress();
        if (identifier == null || !identifier.equals(transaction.getPublicKey())) {
            return Result.error("senderAddress 与 publicKey 必须一致");
        }
        try {
            String senderAddress = transaction.getSenderAddress();
            String recipientAddress = transaction.getRecipientAddress();
            int amount = (int) transaction.getAmount();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .format(transaction.getTimestamp());
            Transaction.Signature signature = transaction.getSignature();
            String publicKeyHex = transaction.getPublicKey();

            Long resourceId = transaction.getResourceId();
            String resourceName = transaction.getResourceName();
            String recordType = transaction.getRecordType();

            // 参数校验
            if (senderAddress == null || senderAddress.isEmpty()) {
                return Result.error("借用者地址不能为空");
            }
            if (recipientAddress == null || recipientAddress.isEmpty()) {
                return Result.error("分享者地址不能为空");
            }
            if (!Transaction.TYPE_RETURN.equals(recordType) && amount <= 0) {
                return Result.error("积分数量必须大于0");
            }
            if (resourceId == null) {
                return Result.error("必须关联一个共享资源");
            }
            if (recordType == null || recordType.isEmpty()) {
                return Result.error("必须指定记录类型");
            }

            // 签名验证
            String message = ECDSAUtil.buildResourceMessage(
                    senderAddress, recipientAddress, amount, timestamp, resourceId, recordType);
            if (publicKeyHex.startsWith("0x")) {
                publicKeyHex = publicKeyHex.substring(2);
            }
            String signatureR = signature.getR();
            String signatureS = signature.getS();
            if (signatureR.length() != 64 || signatureS.length() != 64) {
                return Result.error("签名格式错误");
            }
            boolean isValid = ECDSAUtil.verifySignature(message, signatureR, signatureS, publicKeyHex);
            if (!isValid) {
                return Result.error("签名验证失败");
            }

            log.info("签名验证通过 — 类型: {}, 资源: {}, 积分: {}", recordType, resourceName, amount);

            // 业务处理
            Result businessResult;
            boolean skipChaincode = false;

            if (Transaction.TYPE_BORROW_REQUEST.equals(recordType)) {
                businessResult = handleBorrowRequest(resourceId, senderAddress, amount);
            } else if (Transaction.TYPE_CONFIRM_RECEIVED.equals(recordType)) {
                businessResult = handleConfirmReceived(resourceId, senderAddress, recipientAddress, amount);
            } else if (Transaction.TYPE_RETURN.equals(recordType)) {
                // ========== 归还处理：先检查链上状态，但无论如何都更新数据库 ==========
                // 检查链上状态，决定是否跳过链码调用
                try {
                    String chainResourceJson = fabricCliService.readResource(String.valueOf(resourceId));
                    if (chainResourceJson != null && chainResourceJson.contains("\"status\":\"AVAILABLE\"")) {
                        log.info("资源 {} 在链上已为 AVAILABLE，将仅更新数据库状态，跳过链码调用", resourceId);
                        skipChaincode = true;
                    }
                } catch (Exception e) {
                    log.warn("查询链上资源状态失败，将正常调用链码。resourceId: {}", resourceId, e);
                    // 查询失败时不跳过链码，走正常流程
                }
                // 必须调用 handleReturn 更新数据库状态（状态变为 AVAILABLE，清空 borrowerId）
                businessResult = handleReturn(resourceId);
            } else {
                businessResult = Result.error("未知的记录类型: " + recordType);
            }

            if (businessResult.getCode() != 200) {
                return businessResult;
            }

            // ========== 上链存证（仅当需要时） ==========
            if (!skipChaincode) {
                try {
                    String chainResult = null;
                    if (Transaction.TYPE_CONFIRM_RECEIVED.equals(recordType)) {
                        chainResult = fabricCliService.requestBorrow(String.valueOf(resourceId), senderAddress);
                    } else if (Transaction.TYPE_RETURN.equals(recordType)) {
                        // 二次确认（可选，增强健壮性）
                        String chainState = fabricCliService.readResource(String.valueOf(resourceId));
                        if (chainState != null && chainState.contains("\"status\":\"AVAILABLE\"")) {
                            log.info("资源 {} 在链上已为 AVAILABLE（二次确认），跳过链码调用", resourceId);
                        } else {
                            chainResult = fabricCliService.confirmReturn(String.valueOf(resourceId));
                        }
                    }
                    if (chainResult != null) {
                        log.info("链码存证成功: {}", chainResult);
                    }
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("当前状态为 AVAILABLE")) {
                        log.info("资源 {} 在链上已归还（捕获到状态错误），视为成功", resourceId);
                    } else {
                        log.error("链码存证失败，将回滚业务数据。资源ID: {}, 类型: {}", resourceId, recordType, e);
                        if (Transaction.TYPE_CONFIRM_RECEIVED.equals(recordType)) {
                            rollbackConfirmReceived(resourceId, senderAddress, recipientAddress, amount);
                        } else if (Transaction.TYPE_RETURN.equals(recordType)) {
                            rollbackReturn(resourceId);
                        }
                        throw new RuntimeException("区块链存证失败，事务已回滚: " + e.getMessage(), e);
                    }
                }
            }

            // 保存交易记录到 MySQL
            String txHash = transaction.calculateHash();
            transactionMapper.saveTransactionRecord(
                    senderAddress, recipientAddress, amount, timestamp,
                    resourceId, resourceName,
                    transaction.getResourceType(), recordType,
                    txHash, null
            );

            return Result.success("操作成功");

        } catch (Exception e) {
            log.error("处理交易失败", e);
            return Result.error("交易处理失败：" + e.getMessage());
        }
    }

    private void rollbackConfirmReceived(Long resourceId, String senderAddress, String recipientAddress, int amount) {
        transactionMapper.deductPoints(recipientAddress, amount);
        transactionMapper.addPoints(senderAddress, amount);
        Long borrowerId = userMapper.getUserIdByPublicKey(senderAddress);
        resourceMapper.updateResourceStatus(resourceId, RES_STATUS_PENDING, borrowerId);
        log.info("已回滚确认收货操作: resourceId={}", resourceId);
    }

    private void rollbackReturn(Long resourceId) {
        yin.xuebiblockchain.Pojo.Resource resource = resourceMapper.findById(resourceId);
        if (resource != null && RES_STATUS_AVAILABLE.equals(resource.getStatus())) {
            Long borrowerId = resource.getBorrowerId();
            resourceMapper.updateResourceStatus(resourceId, RES_STATUS_LENT, borrowerId);
            log.info("已回滚归还操作: resourceId={}", resourceId);
        }
    }

    private Result handleBorrowRequest(Long resourceId, String senderAddress, int amount) {
        yin.xuebiblockchain.Pojo.Resource resource = resourceMapper.findById(resourceId);
        if (resource == null) return Result.error("资源不存在");
        if (resource.getOwnerAddress().equals(senderAddress)) return Result.error("不能借用自己发布的资源");
        if (!RES_STATUS_AVAILABLE.equals(resource.getStatus()))
            return Result.error("资源当前不可借用");

        Long balance = transactionMapper.getBalance(senderAddress);
        if (balance == null || balance < amount)
            return Result.error("积分不足");

        Long borrowerId = userMapper.getUserIdByPublicKey(senderAddress);
        resourceMapper.updateResourceStatus(resourceId, RES_STATUS_PENDING, borrowerId);
        resourceMapper.incrementBorrowCount(resourceId);
        return Result.success("借用申请已提交");
    }

    private Result handleConfirmReceived(Long resourceId, String senderAddress,
                                         String recipientAddress, int amount) {
        int deducted = transactionMapper.deductPoints(senderAddress, amount);
        if (deducted == 0) return Result.error("积分扣除失败");
        transactionMapper.addPoints(recipientAddress, amount);

        yin.xuebiblockchain.Pojo.Resource resource = resourceMapper.findById(resourceId);
        if (resource == null) return Result.error("资源不存在");
        Long borrowerId = userMapper.getUserIdByPublicKey(senderAddress);
        Timestamp borrowEndTime = new Timestamp(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000);
        resourceMapper.updateResourceStatusAndBorrowEndTime(resourceId, RES_STATUS_LENT, borrowerId, borrowEndTime);
        return Result.success("确认收到，积分已转移");
    }

    private Result handleReturn(Long resourceId) {
        yin.xuebiblockchain.Pojo.Resource resource = resourceMapper.findById(resourceId);
        if (resource == null) return Result.error("资源不存在");
        if (!RES_STATUS_LENT.equals(resource.getStatus())) return Result.error("资源未被借出");

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (resource.getBorrowEndTime() != null && now.after(resource.getBorrowEndTime())) {
            long overdueMillis = now.getTime() - resource.getBorrowEndTime().getTime();
            long overdueDays = overdueMillis / (24 * 60 * 60 * 1000) + 1;
            int penalty = (int) (overdueDays * 5);
            Long borrowerId = resource.getBorrowerId();
            if (borrowerId != null) {
                String borrowerPublicKey = userMapper.getPublicKeyById(borrowerId);
                if (borrowerPublicKey != null) {
                    transactionMapper.deductPoints(borrowerPublicKey, penalty);
                }
            }
        }
        resourceMapper.updateResourceStatus(resourceId, RES_STATUS_AVAILABLE, null);
        return Result.success("归还成功");
    }

    @Override
    public Result getTransactionHistory(String address, int page, int size) {
        try {
            if (address == null || address.isEmpty()) return Result.error("地址不能为空");
            int offset = (page - 1) * size;
            List<Map<String, Object>> records = transactionMapper.findByAddress(address, size, offset);
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询交易历史失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}