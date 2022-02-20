package com.pluschuh.restclient.spi;

import java.util.Map;

/**
 * 请求对象转换器spi接口
 *
 * @author pluschuh
 */
public interface RequestObjectConverter {

    /**
     * 将作为请求参数或请求体参数的JAVA对象转换成Map格式
     *
     * @param requestObject 作为请求参数或请求体参数的JAVA对象, 不包含基本数据类型及其包装类或String类型
     * @return Map格式的对象
     */
    Map<String, Object> convert(Object requestObject);

}
