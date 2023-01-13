package com.zzd.zrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zzd
 */
@Getter
@AllArgsConstructor
public enum RpcConfigEnum {

    /** key */
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
