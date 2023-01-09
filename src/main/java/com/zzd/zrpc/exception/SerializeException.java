package com.zzd.zrpc.exception;

/**
 * 序列化异常
 *
 * @author zzd
 * @date 2023/1/9
 */
public class SerializeException extends RuntimeException {
    private static final long serialVersionUID = -8486297346811349525L;

    public SerializeException(String message) {
        super(message);
    }
}
