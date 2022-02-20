package com.pluschuh.restclient.spi;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;

/**
 * 默认的restTemplate的响应错误处理器接口
 *
 * @author pluschuh
 */
public interface ResponseErrorHandlerOfDefaultRT extends ResponseErrorHandler {

    /**
     * 是否已处理过错误响应标志，如果是，则最终不会抛出异常
     */
    ThreadLocal<Boolean> ERROR_HANDLED_FLAG = new ThreadLocal<>();

    @Override
    boolean hasError(@Nonnull ClientHttpResponse response) throws IOException;

    @Override
    void handleError(@Nonnull ClientHttpResponse response) throws IOException;

    @Override
    void handleError(@Nonnull URI url, @Nonnull HttpMethod method, @Nonnull ClientHttpResponse response) throws IOException;
}
