package com.zzd.zrpc.annotation;

import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 *
 * @author zzd
 * @date 2023/1/13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
