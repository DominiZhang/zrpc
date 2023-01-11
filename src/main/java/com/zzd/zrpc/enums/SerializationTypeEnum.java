package com.zzd.zrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zzd
 * @date 2023/1/11
 */
@Getter
@AllArgsConstructor
public enum SerializationTypeEnum {

    /** kryo */
    KRYO((byte) 0x01, "kryo"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0x03, "hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum typeEnum : SerializationTypeEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
