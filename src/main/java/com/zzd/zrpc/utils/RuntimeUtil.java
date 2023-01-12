package com.zzd.zrpc.utils;

/**
 * @author zzd
 * @date 2023/1/12
 */
public class RuntimeUtil {

    /**
     * get the number of CPU cores
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
