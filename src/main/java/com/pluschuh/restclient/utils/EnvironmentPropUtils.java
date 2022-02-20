package com.pluschuh.restclient.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

/**
 * 环境配置参数工具类，主要用于获取properties对应的值
 *
 * @author pluschuh
 */
public class EnvironmentPropUtils {

    /**
     * 尝试查找对应的值
     *
     * @param originalVal 原始的值，如果形如 ${a.b.c}则会尝试从环境配置中查找对应的值
     * @param environment 环境对象
     * @return 真实的值，可能为空。如果形如 ${a.b.c}则会尝试从环境配置中查找对应的值，否则返回原始传入的值
     */
    public static String tryFindRealVal(String originalVal, Environment environment) {
        if (StringUtils.isBlank(originalVal)) return StringUtils.EMPTY;
        if (StringUtils.startsWith(originalVal, "${") && StringUtils.endsWith(originalVal, "}")) {
            String propKey = StringUtils.removeStart(originalVal, "${");
            propKey = StringUtils.removeEnd(propKey, "}");
            return environment.getProperty(propKey);
        }

        return originalVal;
    }

    /**
     * 尝试查找对应的值
     *
     * @param originalVal 原始的值，如果形如 ${a.b.c}则会尝试从环境配置中查找对应的值
     * @param environment 环境对象
     * @return 真实的值，如果为空则抛异常。
     */
    public static String findRealVal(String originalVal, Environment environment) {
        String result = tryFindRealVal(originalVal, environment);
        if (StringUtils.isBlank(result))
            throw new RuntimeException("can not find property from environment, please check if the key has been defined with non blank value, " + originalVal);
        return result;
    }
}
