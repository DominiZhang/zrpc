package com.zzd.zrpc.remoting.dto;

import com.zzd.zrpc.enums.RpcResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zzd
 * @date 2023/1/9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 7283144920208292275L;
    /** 请求id，唯一标识符 */
    private String requestId;
    /** status code */
    private Integer code;
    /** error message */
    private String message;
    /** body */
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> resp = new RpcResponse<>();
        resp.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        resp.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        resp.setRequestId(requestId);
        if (null != data) {
            resp.setData(data);
        }
        return resp;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum codeEnum, String message, String requestId) {
        RpcResponse<T> resp = new RpcResponse<>();
        resp.setRequestId(requestId);
        resp.setCode(codeEnum.getCode());
        boolean messageNotEmpty = message != null && message.trim().length() > 0;
        resp.setMessage(messageNotEmpty ? message : codeEnum.getMessage());
        return resp;
    }
}
