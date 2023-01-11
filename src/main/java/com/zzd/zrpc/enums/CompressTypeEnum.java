package com.zzd.zrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zzd
 * @date 2023/1/11
 */
@Getter
@AllArgsConstructor
public enum CompressTypeEnum {

    /** gzip */
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum typeEnum : CompressTypeEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
