package com.pluschuh.restclient.support;

import com.pluschuh.restclient.valueobject.RestClientRequestTemplate;
import org.apache.commons.lang3.StringUtils;

/**
 * REST客户端域名解析器     TODO 后续可考虑load balance实现
 *
 * @author pluschuh
 */
public interface RestClientHostResolver {

    String pathSeparate = "/";

    /**
     * @return 域名
     */
    String host();

    /**
     * 拼装路径获得url，可在该方法中进行load balance等设计
     *
     * @param path 请求路径，如/api/task/等
     * @return url路径，如 http://127.0.0.1:7001/api/task/
     */
    default String appendPath(String path, RestClientRequestTemplate restClientRequestTemplate) {
        String host = host();
        String url;
        if (!StringUtils.endsWith(host, pathSeparate) && !StringUtils.startsWith(path, pathSeparate)) {
            url = host + pathSeparate + path;
        } else {
            url = host + path;
        }
        return url;
    }
}
