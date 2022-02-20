package com.pluschuh.restclient.support;

import com.pluschuh.restclient.valueobject.RestClientRequestTemplate;

/**
 * 请求拦截器，实现类需要注册到SPRING容器中
 *
 * @author pluschuh
 */
public interface RequestInterceptor {

    /**
     * 拦截处理
     *
     * @param requestTemplate 已经经过初步计算后的请求模板对象
     * @param requestMetaInfo 原始请求对象元数据信息，一般用不到
     */
    void apply(RestClientRequestTemplate requestTemplate, RestClientRequestMetaInfo requestMetaInfo);

}
