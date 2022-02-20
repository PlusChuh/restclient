package com.pluschuh.restclient.spi.provide;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluschuh.restclient.spi.RequestObjectConverter;

import java.util.Map;

/**
 * 默认的请求对象转换器实现
 *
 * @author pluschuh
 */
public class DefaultRequestObjectConverter implements RequestObjectConverter {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Map<String, Object> convert(Object requestObject) {
        return MAPPER.convertValue(requestObject, new TypeReference<Map<String, Object>>() {
        });
    }
}
