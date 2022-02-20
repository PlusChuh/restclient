package com.pluschuh.restclient.support;

import com.pluschuh.restclient.spi.ConfigOfDefaultRT;
import com.pluschuh.restclient.spi.ResponseErrorHandlerOfDefaultRT;
import com.pluschuh.restclient.spi.UriTemplateHandlerOfDefaultRT;
import com.pluschuh.restclient.spi.provide.SimpleConfigOfDefaultRT;
import com.pluschuh.restclient.spi.provide.SimpleResponseErrorHandler;
import com.pluschuh.restclient.spi.provide.SimpleUriTemplateHandler;
import com.pluschuh.restclient.valueobject.RestClientSpiProviderValueObject;
import okhttp3.OkHttpClient;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 默认的RestTemplate，
 * 默认情况下每个被@RestClient注解的接口类都会自动创建一个DefaultRestTemplate对象，
 * 该DefaultRestTemplate对象会作为SimpleRestClient的一个成员变量对象
 *
 * @author pluschuh
 */
public class DefaultRestTemplate extends RestTemplate {

    public DefaultRestTemplate(RestClientSpiProviderValueObject restClientSpiProvider, Class<?> restClientInterface) {
        init(restClientSpiProvider, restClientInterface);
    }

    protected void init(RestClientSpiProviderValueObject restClientSpiProvider, Class<?> restClientInterface) {
        ConfigOfDefaultRT configOfDefaultRT
                = SpiProviderHelper.obtainProvider(ConfigOfDefaultRT.class, restClientSpiProvider.configOfDefaultRT(), SimpleConfigOfDefaultRT::new);

        UriTemplateHandlerOfDefaultRT uriTemplateHandlerOfDefaultRT
                = SpiProviderHelper.obtainProvider(UriTemplateHandlerOfDefaultRT.class,
                restClientSpiProvider.uriTemplateHandlerOfDefaultRT(),
                () -> new SimpleUriTemplateHandler(restClientInterface));

        ResponseErrorHandlerOfDefaultRT responseErrorHandlerOfDefaultRT
                = SpiProviderHelper.obtainProvider(ResponseErrorHandlerOfDefaultRT.class,
                restClientSpiProvider.responseErrorHandlerOfDefaultRT(),
                SimpleResponseErrorHandler::new);

        this.setRequestFactory(new OkHttp3ClientHttpRequestFactory(new OkHttpClient.Builder()
                .readTimeout(configOfDefaultRT.readTimeOut(), configOfDefaultRT.readTimeOutUnit())
                .connectTimeout(configOfDefaultRT.connectTimeout(), configOfDefaultRT.connectTimeoutUnit())
                .build()));
        this.setUriTemplateHandler(uriTemplateHandlerOfDefaultRT);
        this.setErrorHandler(responseErrorHandlerOfDefaultRT);
    }
}
