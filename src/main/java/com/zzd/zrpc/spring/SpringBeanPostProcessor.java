package com.zzd.zrpc.spring;

import com.zzd.zrpc.annotation.RpcReference;
import com.zzd.zrpc.annotation.RpcService;
import com.zzd.zrpc.config.RpcServiceConfig;
import com.zzd.zrpc.extension.ExtensionLoader;
import com.zzd.zrpc.factory.SingletonFactory;
import com.zzd.zrpc.provider.ServiceProvider;
import com.zzd.zrpc.provider.impl.ZkServiceProviderImpl;
import com.zzd.zrpc.proxy.RpcClientProxy;
import com.zzd.zrpc.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 需要实现 BeanPostProcessor 接口并重写 postProcessBeforeInitialization() 方法和 postProcessAfterInitialization() 方法
 * Spring bean在实例化之前会调用 postProcessBeforeInitialization() 方法, 在 bean 实例化之后会调用 postProcessAfterInitialization() 方法
 * 可以在 postProcessBeforeInitialization() 方法中去判断类上是否有prcService注解.如果有的化，取出 group 和 version 的值, 然后再调用 serviceProvider 的 publishService() 方法发布服务即可!
 * 我们可以在 postProcessAfterInitialization() 方法中遍历类的属性上是否有 RpcReference 注解, 如果有的话，便通过反射将这个属性赋值即可!
 *
 * @author zzd
 * @date 2023/1/20 23:23
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // build RpcServiceProperties
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
