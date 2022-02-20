package com.pluschuh.restclient.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * REST客户端方法拦截器
 *
 * @author pluschuh
 */
public class RestClientMethodInterceptor implements MethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientMethodInterceptor.class);

    private final Map<Method, MethodHandle> methodHandleCache = new ConcurrentReferenceHashMap<>(10, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final Constructor<MethodHandles.Lookup> lookupConstructor = initLookup();

    @Nonnull
    private final SimpleRestClient simpleRestClient;

    public RestClientMethodInterceptor(@Nonnull SimpleRestClient simpleRestClient) {
        this.simpleRestClient = simpleRestClient;
    }

    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        try {
            Method method = invocation.getMethod();
            Object[] arguments = invocation.getArguments();
            SimpleRestClient.OriginalRestClientMethodInfo originalRestClientMethodInfo = simpleRestClient.findOriginalMethodInfo(method);
            if (Objects.isNull(originalRestClientMethodInfo)) {
                throw new RuntimeException("can not find original rest client method info");
            }

            //被@IgnoreDefalt注释了的default方法
            if (originalRestClientMethodInfo.isIgnoreDefault()) {
                if (Objects.isNull(lookupConstructor)) {
                    LOGGER.warn("construct of MethodHandles.Lookup is null, which means @IgnoreDefault will not be effective, please make sure the program is running with Java8");
                } else {
                    Object proxy = ((ProxyMethodInvocation) invocation).getProxy();
                    return findMethodHandle(method, lookupConstructor).bindTo(proxy).invokeWithArguments(arguments);
                }
            }
            return simpleRestClient.sendRequest(originalRestClientMethodInfo, arguments);
        } catch (Throwable throwable) {
            LOGGER.error("exception when invoke");
            //TODO 定义error handler
            throw throwable;
        }
    }

    /**
     * 查找方法处理器
     *
     * @param method 方法对象
     * @return 方法处理器
     * @throws Exception 任何可能的异常
     */
    private MethodHandle findMethodHandle(Method method, @Nonnull Constructor<MethodHandles.Lookup> constructor) throws Exception {

        MethodHandle handle = methodHandleCache.get(method);
        if (handle == null) {
            // 仅java8环境有效
            handle = constructor.newInstance(method.getDeclaringClass()).unreflectSpecial(method, method.getDeclaringClass());
            methodHandleCache.put(method, handle);
        }

        return handle;
    }

    private static Constructor<MethodHandles.Lookup> initLookup() {
        Constructor<MethodHandles.Lookup> constructor;
        try {
            constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
        } catch (Exception e) {
            LOGGER.debug("hit exception when try to init construct of MethodHandles.Lookup, which means @IgnoreDefault will not be effective");
            return null;
        }
        ReflectionUtils.makeAccessible(constructor);
        return constructor;
    }
}
