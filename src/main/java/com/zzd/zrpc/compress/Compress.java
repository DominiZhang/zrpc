package com.zzd.zrpc.compress;

/**
 * @author zzd
 */
public interface Compress {

    /**
     * compress
     * @param bytes need compress bytes
     * @return compressed bytes
     */
    byte[] compress(byte[] bytes);

    /**
     * decompress
     * @param bytes compressed bytes
     * @return bytes before compress
     */
    byte[] decompress(byte[] bytes);
}
