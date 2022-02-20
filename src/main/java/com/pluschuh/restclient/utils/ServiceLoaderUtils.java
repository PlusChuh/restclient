package com.pluschuh.restclient.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * SPI加载工具
 *
 * @author pluschuh
 */
public class ServiceLoaderUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoaderUtils.class);

    /**
     * 加载所有的SPI接口实现
     *
     * @param interfaceClz 接口类类型
     * @param <T>          泛型
     * @return 一个Map，其中key为接口实现类的类型，value为接口实现类对象
     */
    public static <T> Map<Class<? extends T>, T> loadAll(Class<T> interfaceClz) {
        return loadAllWithDefault(interfaceClz, null, null);
    }

    /**
     * 加载所有的SPI接口实现，当加载出的接口实现对象中不包含默认实现时则将默认实现放入结果中一并返回
     *
     * @param interfaceClz   接口类类型
     * @param defaultImpl    获取默认实现的方法
     * @param defaultImplClz 默认实现类类型
     * @param <T>            接口类泛型
     * @param <S>            实现类泛型
     * @return 一个Map，其中key为接口实现类的类型，value为接口实现类对象
     */
    public static <T, S extends T> Map<Class<? extends T>, T> loadAllWithDefault(Class<T> interfaceClz, Supplier<S> defaultImpl, Class<S> defaultImplClz) {
        Map<Class<? extends T>, T> result = new HashMap<>();
        boolean defaultHasBeenLoaded = false;
        ServiceLoader<T> serviceLoader = ServiceLoader.load(interfaceClz);
        for (T t : serviceLoader) {
            if (Objects.equals(t.getClass(), defaultImplClz)) {
                defaultHasBeenLoaded = true;
            }
            @SuppressWarnings("unchecked")
            Class<? extends T> clz = (Class<? extends T>) t.getClass();
            result.put(clz, t);
        }
        if (!defaultHasBeenLoaded && Objects.nonNull(defaultImpl) && Objects.nonNull(defaultImplClz)) {
            result.put(defaultImplClz, defaultImpl.get());
        }
        return result;
    }

    /**
     * 初始化加载某个接口的spi实现, 当加载为空时则使用默认实现返回
     *
     * @param interfaceClz   接口类类型
     * @param defaultImpl    获取默认实现的方法
     * @param defaultImplClz 默认实现类类型
     * @param <T>            接口类泛型
     * @param <S>            实现类泛型
     * @return 某个接口的的具体实现类对象
     */
    public static <T, S extends T> T init(Class<T> interfaceClz, Supplier<S> defaultImpl, Class<S> defaultImplClz) {
        T result = null;

        ServiceLoader<T> serviceLoader = ServiceLoader.load(interfaceClz);
        T defaultFromLoad = null;
        for (T t : serviceLoader) {
            if (t.getClass().equals(defaultImplClz)) {
                defaultFromLoad = t;
            } else {
                result = t;
                break;
            }
        }
        if (Objects.isNull(result)) {
            LOGGER.info("no spi implementation provided, will use default implementation");
            result = Optional.ofNullable(defaultFromLoad).orElse(defaultImpl.get());
        }
        LOGGER.info("{} has been loaded", result.getClass().getName());
        return result;

    }

}
