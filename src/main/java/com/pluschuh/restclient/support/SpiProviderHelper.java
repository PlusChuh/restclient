package com.pluschuh.restclient.support;

import com.pluschuh.restclient.utils.ServiceLoaderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * SPI提供者处理器，仅限包内部使用
 *
 * @author pluschuh
 */
class SpiProviderHelper {

    private static final Map<Class<?>, SpiProviders<?>> providersOfService = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    static <I, S extends I, D extends I> I obtainProvider(Class<I> interfaceClz, Class<S> providerClz, Supplier<D> defaultImpl) {
        SpiProviders<?> spiProviders = providersOfService.get(interfaceClz);
        if (Objects.isNull(spiProviders)) {
            synchronized (SpiProviderHelper.class) {
                spiProviders = providersOfService.get(interfaceClz);
                if (Objects.isNull(spiProviders)) {
                    Map<Class<? extends I>, I> allProviders = ServiceLoaderUtils.loadAll(interfaceClz);
                    D d = defaultImpl.get();
                    Class<I> defaultImplClz = (Class<I>) d.getClass();
                    I defaultImplFromLoad = allProviders.get(defaultImplClz);
                    if (Objects.isNull(defaultImplFromLoad)) {
                        defaultImplFromLoad = d;
                        allProviders.put(defaultImplClz, d);
                    }
                    spiProviders = new SpiProviders<>(interfaceClz, allProviders, defaultImplFromLoad);
                    providersOfService.put(interfaceClz, spiProviders);
                }
            }
        }
        Object result = spiProviders.getAllProviders().get(providerClz);
        if (Objects.isNull(result)) {
            result = spiProviders.getDefaultImpl();
        }
        return (I) result;
    }

    @Data
    @AllArgsConstructor
    private static class SpiProviders<I> {
        private Class<I> interfaceClz;

        private Map<Class<? extends I>, I> allProviders;

        private I defaultImpl;
    }

}
