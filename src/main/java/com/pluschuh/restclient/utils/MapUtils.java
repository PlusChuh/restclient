package com.pluschuh.restclient.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Map工具类，仅提供部分必要功能
 *
 * @author pluschuh
 */
public class MapUtils {

    public static void removeNullVal(Map<String, Object> map) {
        if (Objects.isNull(map) || map.isEmpty()) return;
        map.entrySet().removeIf(
                next -> Objects.isNull(next.getKey())
                        || Objects.isNull(next.getValue())
                        || Objects.equals(String.valueOf(next.getValue()), "null"));
    }

    public static void removeEmptyVal(Map<String, Object> map) {
        if (Objects.isNull(map) || map.isEmpty()) return;
        map.entrySet().removeIf(
                next -> Objects.isNull(next.getKey())
                        || Objects.isNull(next.getValue())
                        || Objects.equals(String.valueOf(next.getValue()), "null")
                        || StringUtils.isBlank(String.valueOf(next.getValue())));
    }

}
