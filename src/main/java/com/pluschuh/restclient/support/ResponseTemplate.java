package com.pluschuh.restclient.support;

/**
 * 请求响应模板，子类应当保留泛型且不能为抽象类
 *
 * @param <T> 泛型
 * @author pluschuh
 */
public interface ResponseTemplate<T> {

    /**
     * 是否是成功的响应
     *
     * @return 是否是成功的响应
     */
    boolean isFailed();

    /**
     * 真正的响应数据
     *
     * @return 真正的响应数据
     */
    T data();

    default void callBack(RestClientRequestContext restClientRequestContext) {
        if (isFailed()) {
            throw new RuntimeException("call " + restClientRequestContext.getRequestTemplateBeforeInterceptor().getPath() + " with failed response " + this);
        }
    }

}
