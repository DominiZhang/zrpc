package com.zzd.zrpc.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzd
 * @date 2023/1/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcServiceConfig {

    private String version = "";
    /** when the interface has multiple implementation classes, distinguish by group*/
    private String group = "";
    /** target service */
    private Object service;

    public String getRpcServiceName() {
        return this.getRpcServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
