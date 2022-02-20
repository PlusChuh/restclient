package com.pluschuh.restclient.spi;


import com.pluschuh.restclient.valueobject.RestClientRequestTemplate;

/**
 * 请求体序列化器spi接口
 *
 * @author pluschuh
 */
public interface RequestBodySerializer {

    /**
     * 根据请求模板生成请求体对象
     *
     * @param requestTemplate 请求模板
     * @return 请求体对象
     */
    Object serialize(RestClientRequestTemplate requestTemplate);

}
