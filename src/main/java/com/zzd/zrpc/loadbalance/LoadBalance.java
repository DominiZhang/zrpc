package com.zzd.zrpc.loadbalance;

import com.zzd.zrpc.remoting.dto.RpcRequest;

import java.util.List;

/**
 * Interface of the load balancing policy
 *
 * @author zzd
 */
public interface LoadBalance {

    /**
     * Choose one from the list of existing service addresses list
     *
     * @param serviceUrlList Service address list
     * @param rpcRequest rpcRequest
     * @return target service address
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
