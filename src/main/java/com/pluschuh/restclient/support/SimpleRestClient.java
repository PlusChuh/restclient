package com.pluschuh.restclient.support;

import com.pluschuh.restclient.annotation.RestClientRequest;
import com.pluschuh.restclient.spi.RequestBodySerializer;
import com.pluschuh.restclient.spi.RequestObjectConverter;
import com.pluschuh.restclient.spi.RequestParamPathBuilder;
import com.pluschuh.restclient.spi.provide.DefaultRequestBodySerializer;
import com.pluschuh.restclient.spi.provide.DefaultRequestObjectConverter;
import com.pluschuh.restclient.spi.provide.DefaultRequestParamPathBuilder;
import com.pluschuh.restclient.utils.PathVariableUtils;
import com.pluschuh.restclient.valueobject.PathVariableValueObject;
import com.pluschuh.restclient.valueobject.RequestFieldValueObject;
import com.pluschuh.restclient.valueobject.RestClientRequestTemplate;
import com.pluschuh.restclient.valueobject.RestClientSpiProviderValueObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import static com.pluschuh.restclient.spi.ResponseErrorHandlerOfDefaultRT.ERROR_HANDLED_FLAG;

/**
 * 简单REST客户端实现类，核心逻辑。
 * 每个被@RestClient注解的接口类都会自动创建一个SimpleRestClient对象用于代理接口类中所有的方法调用
 *
 * @author pluschuh
 */
public class SimpleRestClient {

    private final RestTemplate restTemplate;

    private final Class<?> metaDataClz;

    private final Logger LOGGER;

    private final RequestObjectConverter requestObjectConverter;

    private final RequestBodySerializer requestBodySerializer;

    private final RequestParamPathBuilder requestParamPathBuilder;

    protected final Map<Method, OriginalRestClientMethodInfo> originalMethodInfos = new HashMap<>();

    static final ThreadLocal<RestClientRequestContext> CURRENT_REQUEST_CONTEXT = new ThreadLocal<>();

    protected void addOriginalMethodInfo(OriginalRestClientMethodInfo originalRestClientMethodInfo) {
        Method method = originalRestClientMethodInfo.getJavaMethod();
        if (Objects.isNull(method) || !Objects.equals(method.getDeclaringClass(), metaDataClz)) {
            throw new RuntimeException("not valid method");
        }
        originalMethodInfos.put(method, originalRestClientMethodInfo);
    }

    public SimpleRestClient(Class<?> metaDataClz, List<OriginalRestClientMethodInfo> originalMethodInfos,
                            RestTemplate restTemplate, RestClientSpiProviderValueObject spiProvider) {
        Assert.notNull(metaDataClz, "metaDataClz must not be null");
        Assert.notNull(originalMethodInfos, "originalMethodInfos must not be null");
        Assert.notNull(restTemplate, "restTemplate must not be null");
        Assert.notNull(spiProvider, "spiProvider must not be null");
        this.metaDataClz = metaDataClz;
        LOGGER = LoggerFactory.getLogger(metaDataClz);
        this.restTemplate = restTemplate;
        this.requestObjectConverter =
                SpiProviderHelper.obtainProvider(RequestObjectConverter.class, spiProvider.requestObjectConvert(), DefaultRequestObjectConverter::new);
        this.requestBodySerializer =
                SpiProviderHelper.obtainProvider(RequestBodySerializer.class, spiProvider.requestBodySerializer(), DefaultRequestBodySerializer::new);
        this.requestParamPathBuilder =
                SpiProviderHelper.obtainProvider(RequestParamPathBuilder.class, spiProvider.requestParamPathBuilder(), DefaultRequestParamPathBuilder::new);
        originalMethodInfos.forEach(this::addOriginalMethodInfo);
    }

    /**
     * 发送请求并获取响应，核心逻辑
     */
    public Object sendRequest(OriginalRestClientMethodInfo originalRestClientMethodInfo, Object[] paramValues) {
        RequestInterceptor requestInterceptor = originalRestClientMethodInfo.getRequestInterceptor();
        RestClientHostResolver hostResolver = originalRestClientMethodInfo.getHostResolver();
        RestClientRequestMetaInfo restClientRequestMetaInfo = originalRestClientMethodInfo.getRestClientRequestMetaInfo();
        long start = System.currentTimeMillis();
        LOGGER.debug("start to parse and sending request ...");
        try {
            RestClientRequestTemplate requestTemplate = buildRequestTemplate(hostResolver, restClientRequestMetaInfo, paramValues);
            RestClientRequestContext restClientRequestContext = RestClientRequestContext.of(originalRestClientMethodInfo, requestTemplate, start);
            CURRENT_REQUEST_CONTEXT.set(restClientRequestContext);
            LOGGER.debug("requestTemplate before interceptor :: {}", requestTemplate);
            LOGGER.debug("withing requestInterceptor :: {}", requestInterceptor.getClass().getName());
            requestInterceptor.apply(requestTemplate, restClientRequestMetaInfo);
            LOGGER.debug("requestTemplate after interceptor :: {}", requestTemplate);
            restClientRequestContext.refreshRequestTemplateAfterInterceptor(requestTemplate);
            //拼装请求
            String fullUrl = buildFullUrl(requestTemplate, hostResolver);
            HttpEntity<?> httpEntity = buildHttpEntity(requestTemplate, restClientRequestMetaInfo);
            @SuppressWarnings("rawtypes")
            Class<? extends ResponseTemplate> responseTemplateType = restClientRequestMetaInfo.getResponseTemplateType();
            Type responseType = restClientRequestMetaInfo.getResponseType();
            LOGGER.debug("trying to {} unexpanded {} with {}", requestTemplate.getHttpMethod(), fullUrl, httpEntity);
            LOGGER.debug("let restTemplate {} to handle it", restTemplate.getClass().getSimpleName());
            if (Modifier.isAbstract(responseTemplateType.getModifiers()) || Modifier.isInterface(responseTemplateType.getModifiers())) {    //TODO 抽象类是否需要排除？
                LOGGER.debug("response templateType {} is an interface or abstract class, will not use it", responseTemplateType.getName());
                LOGGER.debug("will directly use response type {}", responseType);
                ResponseEntity<?> response;
                if ((responseType instanceof Class)) {
                    Class<?> clz = (Class<?>) responseType;
                    response = restTemplate.exchange(fullUrl, restClientRequestMetaInfo.getHttpMethod(), httpEntity, clz);
                } else if (responseType instanceof ParameterizedTypeImpl) {
                    response = restTemplate.exchange(fullUrl, restClientRequestMetaInfo.getHttpMethod(), httpEntity, ParameterizedTypeReference.forType(responseType));
                } else {
                    // 不会走到这里, 因为responseType要么为class要么为ParameterizedTypeImpl
                    response = restTemplate.exchange(fullUrl, restClientRequestMetaInfo.getHttpMethod(), httpEntity, ParameterizedTypeReference.forType(responseType));
                }
                Object body = response.getBody();
                LOGGER.debug("response body is {}", body);
                return body;
            } else {
                LOGGER.debug("response type is {} with templateType {}", responseType, responseTemplateType.getName());
                ParameterizedTypeReference<ResponseTemplate<?>> typeReference = ParameterizedTypeReference.forType(makeType(responseTemplateType, responseType));
                ResponseEntity<ResponseTemplate<?>> response =
                        restTemplate.exchange(fullUrl, restClientRequestMetaInfo.getHttpMethod(), httpEntity, typeReference);
                ResponseTemplate<?> responseTemplate = response.getBody();
                LOGGER.debug("response body is {}", responseTemplate);
                if (Objects.isNull(responseTemplate)) {
                    LOGGER.warn("responseTemplate is null, will return null");
                    return null;
                }
                LOGGER.debug("start to callback responseTemplate");
                responseTemplate.callBack(restClientRequestContext);
                LOGGER.debug("callback done, will return data in responseTemplate");
                return responseTemplate.data();
            }
        } catch (Throwable ex) {
            Boolean errorHandledFlag = ERROR_HANDLED_FLAG.get();
            //如果在http的响应中有错误，则一般在extractData时也会有异常，此处只要处理过异常，则不再抛出
            if (Objects.nonNull(errorHandledFlag) && errorHandledFlag) {
                LOGGER.error("response error already caught, will directly return null");
                return null;
            } else {
                LOGGER.error("exception when call api with rest client {}", metaDataClz.getSimpleName(), ex);
                throw ex;
            }
        } finally {
            CURRENT_REQUEST_CONTEXT.remove();
            ERROR_HANDLED_FLAG.remove();
            long end = System.currentTimeMillis();
            LOGGER.debug("end of send request, cost {} ms ...", end - start);
        }

    }

    public OriginalRestClientMethodInfo findOriginalMethodInfo(Method method) {
        return originalMethodInfos.get(method);
    }

    /**
     * 构造用于ParameterizedTypeReference的Type对象
     *
     * @param rawType 原始类型
     * @param dtoType DTO类型
     * @return Type对象
     */
    private Type makeType(Class<?> rawType, Type dtoType) {
        Type[] actualTypeArguments = new Type[1];
        actualTypeArguments[0] = dtoType;
        return ParameterizedTypeImpl.make(rawType, actualTypeArguments, null);
    }

    private HttpEntity<?> buildHttpEntity(RestClientRequestTemplate requestTemplate, RestClientRequestMetaInfo restClientRequestMetaInfo) {
        Object body = null;
        if (!Objects.equals(requestTemplate.getHttpMethod(), HttpMethod.GET)
                && !Objects.equals(requestTemplate.getHttpMethod(), HttpMethod.DELETE)) {
            body = serializeBody(requestTemplate);
        }
        return new HttpEntity<>(body, buildHeaders(requestTemplate, restClientRequestMetaInfo));
    }

    private HttpHeaders buildHeaders(RestClientRequestTemplate requestTemplate, RestClientRequestMetaInfo restClientRequestMetaInfo) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, Object> requestHeaderValueMap = requestTemplate.getRequestHeader();

        for (String headerName : requestHeaderValueMap.keySet()) {
            Object headerValue = requestHeaderValueMap.get(headerName);
            if (StringUtils.isNotBlank(headerName) && Objects.nonNull(headerValue)) {
                httpHeaders.add(headerName, String.valueOf(headerValue));
            }
        }

        if (!httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            String contentType = restClientRequestMetaInfo.getContentType();
            if (StringUtils.isBlank(contentType)) {
                contentType = MediaType.APPLICATION_JSON_VALUE;
            }
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        }
        return httpHeaders;
    }

    private Object serializeBody(RestClientRequestTemplate requestTemplate) {
        return requestBodySerializer.serialize(requestTemplate);
    }

    private static final String QUESTION_MARK = "?";

    private String buildFullUrl(RestClientRequestTemplate restClientRequestTemplate, RestClientHostResolver hostResolver) {
        String url = hostResolver.appendPath(restClientRequestTemplate.getPath(), restClientRequestTemplate);
        String paramPath = this.requestParamPathBuilder.buildPathOfParams(restClientRequestTemplate.getRequestParam());
        if (StringUtils.isBlank(paramPath)) {
            return url;
        }

        if (!StringUtils.startsWith(paramPath, QUESTION_MARK) && !StringUtils.endsWith(url, QUESTION_MARK)) {
            return url + QUESTION_MARK + paramPath;
        }

        return url + paramPath;
    }

    private RestClientRequestTemplate buildRequestTemplate(RestClientHostResolver hostResolver, RestClientRequestMetaInfo restClientRequestMetaInfo, Object[] paramValues) {
        // String originalUrl = hostResolver.host() + restClientRequestMetaInfo.getPath();
        String contentType = restClientRequestMetaInfo.getContentType();
        Map<String, Object> requestHeader = new HashMap<>();
        requestHeader.put(HttpHeaders.CONTENT_TYPE, contentType);
        List<RequestFieldValueObject> requestFieldEntities = restClientRequestMetaInfo.getOrderedRequestFields();
        if (Objects.isNull(requestFieldEntities) || requestFieldEntities.isEmpty()) {
            return new RestClientRequestTemplate(hostResolver.host(), restClientRequestMetaInfo.getPath(), restClientRequestMetaInfo.getHttpMethod(),
                    null, null, requestHeader);
        }

        if (requestFieldEntities.size() != paramValues.length) {
            throw new RuntimeException("not valid params"); //TODO
        }
        Map<RequestFieldValueObject, Object> requestParamValueMap = new HashMap<>();
        for (int i = 0; i < requestFieldEntities.size(); i++) {
            requestParamValueMap.put(requestFieldEntities.get(i), paramValues[i]);
        }

        //find url
        List<RequestFieldValueObject> pathVariableParams = Optional.ofNullable(restClientRequestMetaInfo.getPathVariableRequestFields()).orElse(new ArrayList<>());
        List<PathVariableValueObject> pathVariableValues = new ArrayList<>();
        for (RequestFieldValueObject pathVariableParam : pathVariableParams) {
            Object pathVariableValue = requestParamValueMap.get(pathVariableParam);
            if (Objects.isNull(pathVariableValue)) {
                continue;
            }
            pathVariableValues.add(new PathVariableValueObject(pathVariableParam.getName(), String.valueOf(pathVariableValue)));
        }
        String finalPath = PathVariableUtils.replacePathVariables(restClientRequestMetaInfo.getPath(), pathVariableValues);

        //find request param and request body
        Map<String, Object> requestParam = transferRequestFieldEntity2Map(restClientRequestMetaInfo.getQueryRequestFields(), requestParamValueMap);
        Map<String, Object> requestBody = transferRequestFieldEntity2Map(restClientRequestMetaInfo.getBodyRequestFields(), requestParamValueMap);
        Map<String, Object> requestHeaderValueMap = transferRequestFieldEntity2Map(restClientRequestMetaInfo.getHeaderRequestFields(), requestParamValueMap);
        requestHeader.putAll(requestHeaderValueMap);

        //检查路径中是否仍存在尚未替换的参数
        List<String> pathVariableUnReplacedList = PathVariableUtils.tryFindPathVariableNames(finalPath);
        //如果仍存在则从requestParam或requestBody中查找是否有相应的值
        if (!pathVariableUnReplacedList.isEmpty()) {
            List<PathVariableValueObject> unReplacedPathVariableValues = new ArrayList<>();
            for (String pathVariableUnReplaced : pathVariableUnReplacedList) {
                Object unReplacedPathVariableValue = requestParam.get(pathVariableUnReplaced);
                if (Objects.isNull(unReplacedPathVariableValue) || StringUtils.equals(unReplacedPathVariableValue.toString(), "null")) {
                    unReplacedPathVariableValue = requestBody.get(pathVariableUnReplaced);
                }
                unReplacedPathVariableValues.add(new PathVariableValueObject(pathVariableUnReplaced, String.valueOf(unReplacedPathVariableValue)));
            }
            finalPath = PathVariableUtils.replacePathVariables(finalPath, unReplacedPathVariableValues);
        }

        return new RestClientRequestTemplate(hostResolver.host(), finalPath, restClientRequestMetaInfo.getHttpMethod(),
                requestParam, requestBody, requestHeader);
    }

    private Map<String, Object> transferRequestFieldEntity2Map(List<RequestFieldValueObject> requestFieldEntities, Map<RequestFieldValueObject, Object> requestParamValueMap) {
        Map<String, Object> result = new HashMap<>();
        if (Objects.nonNull(requestFieldEntities) && !requestFieldEntities.isEmpty()) {
            for (RequestFieldValueObject paramField : requestFieldEntities) {
                Object value = requestParamValueMap.get(paramField);
                if (paramField.isJsonFormatAble()) {
                    Map<String, Object> params;
                    if (value instanceof RequestFieldJsonFormatAble) {
                        params = ((RequestFieldJsonFormatAble) value).asMap(this.requestObjectConverter);
                    } else {
                        params = this.requestObjectConverter.convert(value);
                    }

                    params = Optional.ofNullable(params).orElse(new HashMap<>());
                    result.putAll(params);
                } else {
                    result.put(paramField.getName(), value);
                }
            }
        }
        return result;
    }

    @Data
    public static class OriginalRestClientMethodInfo {
        private final Method javaMethod;
        private final RestClientRequestMetaInfo restClientRequestMetaInfo;
        private final RequestInterceptor requestInterceptor;
        private final RestClientHostResolver hostResolver;
        private final boolean ignoreDefault;

        public OriginalRestClientMethodInfo(Method javaMethod, RestClientRequestMetaInfo restClientRequestMetaInfo,
                                            RequestInterceptor requestInterceptor, RestClientHostResolver hostResolver) {
            Assert.notNull(javaMethod, "javaMethod must not be null");
            Assert.notNull(restClientRequestMetaInfo, "restClientRequestMetaInfo must not be null");
            Assert.notNull(requestInterceptor, "requestInterceptor must not be null");
            Assert.notNull(hostResolver, "hostResolver must not be null");
            this.javaMethod = javaMethod;
            this.restClientRequestMetaInfo = restClientRequestMetaInfo;
            this.requestInterceptor = requestInterceptor;
            this.hostResolver = hostResolver;
            RestClientRequest restClientRequest = javaMethod.getAnnotation(RestClientRequest.class);
            ignoreDefault = Objects.isNull(restClientRequest) && javaMethod.isDefault();
        }
    }

}
