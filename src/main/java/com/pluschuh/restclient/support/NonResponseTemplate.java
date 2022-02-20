package com.pluschuh.restclient.support;

/**
 * 标记接口，仅用于标记某个rest客户端的请求响应不会被以响应模板方式解析
 *
 * @param <T> 泛型
 * @author pluschuh
 */
public interface NonResponseTemplate<T> extends ResponseTemplate<T> {
}
