package com.zzd.zrpc.extension;

import com.zzd.zrpc.utils.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzd
 * @date 2023/1/11
 */
public class ExtensionLoader<T> {
    private static final String SERVICE_DIRECTORY = "META-INF/extensions";
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADER_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES_MAP = new ConcurrentHashMap<>();

    private final Class<?> type;
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // first get from cache, if not hit, create one
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADER_MAP.get(type);
        if (null == extensionLoader) {
            EXTENSION_LOADER_MAP.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADER_MAP.get(type);
        }
        return extensionLoader;
    }

    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // first get from cache, if not hit, create one also
        Holder<Object> holder = cachedInstances.get(name);
        if (null == holder) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // create a singleton if no instance exists
        Object instance = holder.get();
        if (null == instance) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    // TODO
//                    instance =
                    holder.set(instance);
                }
            } // synchronized
        }
        return (T) instance;
    }
}
