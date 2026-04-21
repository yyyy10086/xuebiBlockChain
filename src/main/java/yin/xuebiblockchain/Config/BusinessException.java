package yin.xuebiblockchain.Config;

/**
 * 自定义业务异常
 * 用于 Service 层主动抛出可预期的业务错误
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}