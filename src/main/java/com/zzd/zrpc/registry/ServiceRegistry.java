package com.zzd.zrpc.registry;

import com.zzd.zrpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author zzd
 */
@SPI
public interface ServiceRegistry {
    /**
     * register service
     *
     * @param rpcServiceName    rpc service name
     * @param inetSocketAddress service address
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
