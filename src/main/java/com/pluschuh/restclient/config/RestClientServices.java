package com.pluschuh.restclient.config;

import com.pluschuh.restclient.constants.RestClientCommonConstant;
import com.pluschuh.restclient.spi.provide.EnhancedResponseErrorHandler;
import com.pluschuh.restclient.spi.provide.EnhancedResponseErrorHandler.Receiver;
import com.pluschuh.restclient.spi.provide.EnhancedResponseErrorHandler.EnhancedResponseErrorHandlerService;
import com.pluschuh.restclient.spi.provide.SimpleUriTemplateHandler;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * rest客户端组件默认提供的服务定义
 *
 * @author pluschuh
 */
class RestClientServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientServices.class);

    public RestClientServices() {
        LOGGER.info("RestClientServices constructed");
    }

    @Bean("defaultEnhancedResponseErrorHandlerService")
    public EnhancedResponseErrorHandler.EnhancedResponseErrorHandlerService enhancedResponseErrorHandlerService(
            @Autowired(required = false) List<Receiver> errorHandlers) {
        EnhancedResponseErrorHandlerService enhancedResponseErrorHandlerService = new EnhancedResponseErrorHandlerService(errorHandlers);
        initEnhancedResponseErrorHandler(enhancedResponseErrorHandlerService);
        return enhancedResponseErrorHandlerService;
    }

    private void initEnhancedResponseErrorHandler(EnhancedResponseErrorHandlerService springService) {
        //初始化innerSpringService
        try {
            Class<EnhancedResponseErrorHandler> clz = EnhancedResponseErrorHandler.class;
            Method setInnerSpringService = clz.getDeclaredMethod("setInnerSpringService", EnhancedResponseErrorHandlerService.class);
            setInnerSpringService.setAccessible(true);
            setInnerSpringService.invoke(null, springService);
        } catch (Throwable e) {
            LOGGER.error("failed to init inner spring service of EnhancedResponseErrorHandler", e);
        }
    }

    @Bean(RestClientCommonConstant.defaultSpringRestTemplateBeanName)
    public RestTemplate defaultSpringRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        SimpleUriTemplateHandler uriTemplateHandler = new SimpleUriTemplateHandler(SimpleUriTemplateHandler.class);
        EnhancedResponseErrorHandler enhancedResponseErrorHandler = new EnhancedResponseErrorHandler();
        restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build()));
        restTemplate.setUriTemplateHandler(uriTemplateHandler);
        restTemplate.setErrorHandler(enhancedResponseErrorHandler);
        return restTemplate;
    }

}
