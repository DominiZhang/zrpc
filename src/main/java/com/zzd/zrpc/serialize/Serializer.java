package com.zzd.zrpc.serialize;

import com.zzd.zrpc.extension.SPI;

/**
 * @author zzd
 * @date 2023/1/9
 */
@SPI
public interface Serializer {
    /**
     * 序列化
     * @param obj 需要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T> 反序列化后的类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
