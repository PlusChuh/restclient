package com.pluschuh.restclient.support;

import com.pluschuh.restclient.enums.RequestFieldType;
import com.pluschuh.restclient.valueobject.RequestFieldValueObject;
import org.springframework.http.HttpMethod;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.*;

/**
 * REST客户端请求原始信息
 *
 * @author pluschuh
 */
public class RestClientRequestMetaInfo {

    /**
     * 原始请求路径，如 /api/task，/api/product/{productId}/detail等
     */
    private String path;

    private String contentType;

    /**
     * 请求方式
     */
    private HttpMethod httpMethod;

    private Type responseType;

    @SuppressWarnings("rawtypes")
    private Class<? extends ResponseTemplate> responseTemplateType;

    private Class<? extends RequestInterceptor> requestInterceptor;

    private Class<? extends RestClientHostResolver> hostResolver;

    private final LinkedList<RequestFieldValueObject> orderedRequestFields = new LinkedList<>();
    private final List<RequestFieldValueObject> pathVariableRequestFields = new LinkedList<>();
    private final List<RequestFieldValueObject> queryRequestFields = new LinkedList<>();
    private final List<RequestFieldValueObject> bodyRequestFields = new LinkedList<>();
    private final List<RequestFieldValueObject> headerRequestFields = new LinkedList<>();

    public void addOrderedRequestField(@Nonnull RequestFieldValueObject requestFieldValueObject) {
        orderedRequestFields.add(requestFieldValueObject);
        RequestFieldType requestFieldType = requestFieldValueObject.getType();
        if (Objects.equals(requestFieldType, RequestFieldType.BODY)) {
            bodyRequestFields.add(requestFieldValueObject);
        } else if (Objects.equals(requestFieldType, RequestFieldType.QUERY)) {
            queryRequestFields.add(requestFieldValueObject);
        } else if (Objects.equals(requestFieldType, RequestFieldType.PATH_VARIABLE)) {
            pathVariableRequestFields.add(requestFieldValueObject);
        } else if (Objects.equals(requestFieldType, RequestFieldType.HEADER)) {
            headerRequestFields.add(requestFieldValueObject);
        }
    }

    public RestClientRequestMetaInfo() {
    }

    @SuppressWarnings("rawtypes")
    public RestClientRequestMetaInfo(String path, String contentType, HttpMethod httpMethod, Type responseType,
                                     Class<? extends ResponseTemplate> responseTemplateType,
                                     Class<? extends RequestInterceptor> requestInterceptor,
                                     Class<? extends RestClientHostResolver> hostResolver,
                                     List<RequestFieldValueObject> requestFieldValueObjects) {
        this.path = path;
        this.contentType = contentType;
        this.httpMethod = httpMethod;
        this.responseType = responseType;
        this.responseTemplateType = responseTemplateType;
        this.requestInterceptor = requestInterceptor;
        this.hostResolver = hostResolver;
        Optional.ofNullable(requestFieldValueObjects).ifPresent(requestFieldValueObjects1 -> requestFieldValueObjects1.forEach(this::addOrderedRequestField));
    }

    public String getPath() {
        return path;
    }

    public String getContentType() {
        return contentType;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Type getResponseType() {
        return responseType;
    }

    @SuppressWarnings("rawtypes")
    public Class<? extends ResponseTemplate> getResponseTemplateType() {
        return responseTemplateType;
    }

    public Class<? extends RequestInterceptor> getRequestInterceptor() {
        return requestInterceptor;
    }

    public Class<? extends RestClientHostResolver> getHostResolver() {
        return hostResolver;
    }

    public List<RequestFieldValueObject> getOrderedRequestFields() {
        return Collections.unmodifiableList(orderedRequestFields);
    }

    public List<RequestFieldValueObject> getPathVariableRequestFields() {
        return Collections.unmodifiableList(pathVariableRequestFields);
    }

    public List<RequestFieldValueObject> getQueryRequestFields() {
        return Collections.unmodifiableList(queryRequestFields);
    }

    public List<RequestFieldValueObject> getBodyRequestFields() {
        return Collections.unmodifiableList(bodyRequestFields);
    }

    public List<RequestFieldValueObject> getHeaderRequestFields() {
        return Collections.unmodifiableList(headerRequestFields);
    }

    void setContentType(String contentType) {
        this.contentType = contentType;
    }

    void setHttpMethod(@Nonnull HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    void setResponseType(@Nonnull Type responseType) {
        this.responseType = responseType;
    }

    @SuppressWarnings("rawtypes")
    void setResponseTemplateType(Class<? extends ResponseTemplate> responseTemplateType) {
        this.responseTemplateType = responseTemplateType;
    }

    void setRequestInterceptor(Class<? extends RequestInterceptor> requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }

    void setPath(String path) {
        this.path = path;
    }

    void setHostResolver(Class<? extends RestClientHostResolver> hostResolver) {
        this.hostResolver = hostResolver;
    }
}
