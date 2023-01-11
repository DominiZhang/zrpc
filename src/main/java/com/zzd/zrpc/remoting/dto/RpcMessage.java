package com.zzd.zrpc.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzd
 * @date 2023/1/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcMessage {

    private byte messageType;
    /** serialization type */
    private byte codec;
    private byte compress;
    private int requestId;
    private Object data;
}
