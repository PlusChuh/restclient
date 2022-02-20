package com.pluschuh.restclient.valueobject;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * REST客户端请求模板对象
 *
 * @author pluschuh
 */
@Getter
@ToString
public class RestClientRequestTemplate {

    public static final RestClientRequestTemplate NULL = new RestClientRequestTemplate(StringUtils.EMPTY,
            StringUtils.EMPTY,
            HttpMethod.GET,
            null,
            null,
            null);

    private final String host;
    private final String path;
    private final HttpMethod httpMethod;

    @Nonnull
    private final Map<String, Object> requestParam;

    @Nonnull
    private final Map<String, Object> requestBody;

    @Nonnull
    private final Map<String, Object> requestHeader;

    public RestClientRequestTemplate(String host, String path, HttpMethod httpMethod,
                                     Map<String, Object> requestParam,
                                     Map<String, Object> requestBody,
                                     Map<String, Object> requestHeader) {
        this.host = host;
        this.path = path;
        this.httpMethod = httpMethod;
        this.requestParam = Optional.ofNullable(requestParam).orElse(new HashMap<>());
        this.requestBody = Optional.ofNullable(requestBody).orElse(new HashMap<>());
        this.requestHeader = Optional.ofNullable(requestHeader).orElseGet(() -> {
            Map<String, Object> httpHeaders = new HashMap<>();
            httpHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return httpHeaders;
        });
    }

    public static RestClientRequestTemplate copy(RestClientRequestTemplate source) {
        if (Objects.isNull(source)) return null;
        return new RestClientRequestTemplate(source.getHost(),
                source.getPath(),
                source.getHttpMethod(),
                Maps.newHashMap(source.getRequestParam()),
                Maps.newHashMap(source.getRequestBody()),
                Maps.newHashMap(source.getRequestHeader()));
    }
}
