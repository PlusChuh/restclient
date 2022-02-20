package com.pluschuh.restclient.spi;

import org.springframework.lang.NonNull;

import java.util.Map;

/**
 *  请求参数路径构造接口。
 *  该接口主要用于拼装完整的请求路径，如原始的请求路径为 http://abc.cde.efg/task，
 *  调用该接口方法后得到 page=1&pageSize=25&keyword=test，
 *  则最终的请求路径为 http://abc.cde.efg/task?page=1&pageSize=25&keyword=test
 *
 * @author pluschuh
 */
public interface RequestParamPathBuilder {

    /**
     * 构建请求路径中的参数相关路径
     *
     * @param requestParams 参数对象
     * @return 请求路径中的参数相关路径，如 page=1&pageSize=25&keyword=test
     */
    String buildPathOfParams(@NonNull Map<String, Object> requestParams);

}
