package com.zzd.zrpc.registry;

import com.zzd.zrpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @author zzd
 * @date 2023/1/11
 */
public interface ServiceDiscovery {
    /**
     * lookup service by rpcServiceName
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
