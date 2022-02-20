package com.pluschuh.restclient.support;

import org.apache.commons.lang3.StringUtils;

/**
 * 默认的域名处理器类，针对每一个RestClient默认会新建一个对应的默认域名处理器对象
 *
 * @author pluschuh
 */
public class DefaultRestClientHostResolver implements RestClientHostResolver {

    /**
     * 域名
     */
    private final String host;

    private static final String DEFAULT_LOCAL_HOST = "http://127.0.0.1:80";

    /**
     * 构造器
     *
     * @param host 域名，来自@RestClient中的host
     */
    public DefaultRestClientHostResolver(String host) {
        if (StringUtils.isBlank(host)) {
            host = DEFAULT_LOCAL_HOST;
        }
        this.host = host;
    }

    @Override
    public String host() {
        return host;
    }
}
