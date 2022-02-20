package com.pluschuh.restclient.support;

import com.pluschuh.restclient.valueobject.RestClientRequestTemplate;
import lombok.Data;

/**
 * rest客户端请求上下文
 *
 * @author pluschuh
 */
@Data
public class RestClientRequestContext {

    private SimpleRestClient.OriginalRestClientMethodInfo originalRestClientMethodInfo;
    private final RestClientRequestTemplate requestTemplateBeforeInterceptor;
    private RestClientRequestTemplate requestTemplateAfterInterceptor;
    private final long startTimeMillis;

    private static final RestClientRequestTemplate PENDING = RestClientRequestTemplate.copy(RestClientRequestTemplate.NULL);

    RestClientRequestContext(SimpleRestClient.OriginalRestClientMethodInfo originalRestClientMethodInfo,
                             RestClientRequestTemplate requestTemplateBeforeInterceptor,
                             long startTimeMillis) {
        this.originalRestClientMethodInfo = originalRestClientMethodInfo;
        this.requestTemplateBeforeInterceptor = RestClientRequestTemplate.copy(requestTemplateBeforeInterceptor);
        this.requestTemplateAfterInterceptor = PENDING;
        this.startTimeMillis = startTimeMillis;
    }

    void refreshRequestTemplateAfterInterceptor(RestClientRequestTemplate requestTemplateAfterInterceptor) {
        this.requestTemplateAfterInterceptor = RestClientRequestTemplate.copy(requestTemplateAfterInterceptor);
    }

    static RestClientRequestContext of(SimpleRestClient.OriginalRestClientMethodInfo originalRestClientMethodInfo,
                                       RestClientRequestTemplate requestTemplateBeforeInterceptor,
                                       long startTimeMillis) {
        return new RestClientRequestContext(originalRestClientMethodInfo,
                requestTemplateBeforeInterceptor,
                startTimeMillis);
    }


}
