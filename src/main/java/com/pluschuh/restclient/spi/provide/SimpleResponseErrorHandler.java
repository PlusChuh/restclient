package com.pluschuh.restclient.spi.provide;

import com.pluschuh.restclient.spi.ResponseErrorHandlerOfDefaultRT;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;

/**
 * 简单响应处理器，实际逻辑与spring提供的DefaultResponseErrorHandler保持一致。
 * 使用方可根据需要自行进行spi替换。
 *
 * @author pluschuh
 */
public class SimpleResponseErrorHandler extends DefaultResponseErrorHandler implements ResponseErrorHandlerOfDefaultRT {

    /**
     * 抛出异常，子类可自行实现该接口，如果未抛出则通过restclient调用得到的接口会为null
     */
    protected void throwError(@Nonnull URI url, @Nonnull HttpMethod method, @Nonnull ClientHttpResponse response) throws IOException {
        super.handleError(url, method, response);
    }

    @Override
    public void handleError(@Nonnull URI url, @Nonnull HttpMethod method, @Nonnull ClientHttpResponse response) throws IOException {
        throwError(url, method, response);
        ERROR_HANDLED_FLAG.set(true);
    }
}
