package com.zzd.zrpc.extension;

/**
 * @author zzd
 * @date 2023/1/11
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
