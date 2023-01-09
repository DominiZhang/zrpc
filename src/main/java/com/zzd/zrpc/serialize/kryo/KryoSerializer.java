package com.zzd.zrpc.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.zzd.zrpc.exception.SerializeException;
import com.zzd.zrpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo serialization class, Kryo serialization efficiency is very high, but only compatible with Java language
 *
 * @author zzd
 * @date 2023/1/9
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * Use ThreadLocal to store Kryo objects, because Kryo is not thread safe.
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
//        kryo.register(RecResponse.class);
//        kryo.register(RecRequest.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output out = new Output(bos)) {
            Kryo kryo = kryoThreadLocal.get();
            // object -> byte: 将对象序列化为byte数组
            kryo.writeObject(out, obj);
            // 使用完后便移除，及时清理
            kryoThreadLocal.remove();
            return out.toBytes();
        } catch (Exception e) {
            throw new SerializeException("serialize failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream ios = new ByteArrayInputStream(bytes);
             Input in = new Input(ios)) {
            Kryo kryo = kryoThreadLocal.get();
            T t = kryo.readObject(in, clazz);
            kryoThreadLocal.remove();
            return t;
        } catch (Exception e) {
            throw new SerializeException("deserialize failed");
        }
    }
}
