package com.zzd.zrpc.utils;

import java.util.Collection;

/**
 * @author zzd
 * @date 2023/1/13
 */
public class CollectionUtil {

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }
}
