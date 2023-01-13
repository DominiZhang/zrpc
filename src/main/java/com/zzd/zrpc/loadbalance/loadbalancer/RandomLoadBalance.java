package com.zzd.zrpc.loadbalance.loadbalancer;

import com.zzd.zrpc.loadbalance.AbstractLoadBalance;
import com.zzd.zrpc.remoting.dto.RpcRequest;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;

import java.util.List;
import java.util.Random;

/**
 * random load balancing strategy
 *
 * @author zzd
 * @date 2023/1/13
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
