package com.zzd.zrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author zzd
 * @date 2023/1/9
 */
@Getter
@ToString
@AllArgsConstructor
public enum RpcResponseCodeEnum  {
    /** 成功 */
    SUCCESS(200, "The remote call is success"),
    /** 服务器报错失败调用 */
    FAIL(500, "The remote call is fail");

    private final Integer code;
    private final String message;
}
