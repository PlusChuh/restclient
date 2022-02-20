package com.pluschuh.restclient.support;

import com.pluschuh.restclient.utils.MapUtils;
import com.pluschuh.restclient.valueobject.RestClientRequestTemplate;

import java.util.Map;

/**
 * 过滤QUERY请求参数中的空值
 *
 * @author pluschuh
 */
public class RemoveNullQueryRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RestClientRequestTemplate requestTemplate, RestClientRequestMetaInfo requestMetaInfo) {
        Map<String, Object> requestParam = requestTemplate.getRequestParam();
        Map<String, Object> requestHeader = requestTemplate.getRequestHeader();
        MapUtils.removeEmptyVal(requestParam);
        MapUtils.removeEmptyVal(requestHeader);
    }
}
