package com.zzd.zrpc.exception;

/**
 * 序列化异常
 *
 * @author zzd
 * @date 2023/1/9
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String message) {
        super(message);
    }
}
