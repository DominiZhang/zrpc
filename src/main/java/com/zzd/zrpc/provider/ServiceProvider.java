package com.zzd.zrpc.provider;

import com.zzd.zrpc.config.RpcServiceConfig;

/**
 * store and provide service object.
 *
 * @author zzd
 */
public interface ServiceProvider {


    /**
     * addService
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * getService
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     * publish service
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
