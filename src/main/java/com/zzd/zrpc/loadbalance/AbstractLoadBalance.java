package com.zzd.zrpc.loadbalance;

import com.zzd.zrpc.remoting.dto.RpcRequest;
import com.zzd.zrpc.utils.CollectionUtil;

import java.util.List;

/**
 * Abstract class for a load balancing policy
 *
 * @author zzd
 * @date 2023/1/13
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            return null;
        }
        if (serviceUrlList.size() == 1) {
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList, rpcRequest);
    }

    /**
     * doSelect
     * @param serviceUrlList url list
     * @param rpcRequest request
     * @return service url
     */
    protected abstract String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest);
}
