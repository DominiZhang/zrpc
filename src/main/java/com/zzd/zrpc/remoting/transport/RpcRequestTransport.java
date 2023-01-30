package com.zzd.zrpc.remoting.transport;

import com.zzd.zrpc.extension.SPI;
import com.zzd.zrpc.remoting.dto.RpcRequest;

/**
 * 发送RpcRequest请求接口类
 *
 * @author zzd
 * @date 2023/1/10
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send rpcRequest to server and get result
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
