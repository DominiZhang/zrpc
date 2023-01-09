package com.zzd.zrpc.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author zzd
 * @date 2023/1/9
 */
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 9179518921906850511L;
    /** 请求id，唯一标识符 */
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.interfaceName + this.group + this.version;
    }
}
