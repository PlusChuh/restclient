package com.pluschuh.restclient.support;

import com.pluschuh.restclient.annotation.RequestField;
import com.pluschuh.restclient.annotation.RestClient;
import com.pluschuh.restclient.annotation.RestClientRequest;
import com.pluschuh.restclient.annotation.RestClientSpiProvider;
import com.pluschuh.restclient.enums.RequestFieldType;
import com.pluschuh.restclient.utils.EnvironmentPropUtils;
import com.pluschuh.restclient.valueobject.RequestFieldValueObject;
import com.pluschuh.restclient.valueobject.RestClientSpiProviderValueObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * rest客户端工厂类
 *
 * @param <T> 泛型
 * @author pluschuh
 */
public class RestClientFactory<T> implements FactoryBean<T>, InitializingBean, ApplicationContextAware, EnvironmentAware {

    private final Class<?> restClientInterface;

    private T restClient;

    private final RestClient restClientAnnotation;

    private static final RequestInterceptor defaultRequestInterceptor = new DefaultRequestInterceptor();

    private ApplicationContext applicationContext;
    private Environment environment;

    /**
     * 方法参数名发现器，当使用在接口的非default方法时无实际作用
     */
    private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private static final String URL_PATH_SEPARATOR = "/";

    public RestClientFactory(Class<T> restClientInterface, RestClient restClientAnnotation) {
        if (Objects.isNull(restClientInterface) || !Modifier.isInterface(restClientInterface.getModifiers()) || Objects.isNull(restClientAnnotation)) {
            throw new RuntimeException("not valid rest client definition");
        }
        this.restClientInterface = restClientInterface;
        this.restClientAnnotation = restClientAnnotation;
    }

    @Override
    public T getObject() {
        return restClient;
    }

    @Override
    public Class<?> getObjectType() {
        return restClientInterface;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(@Nonnull Environment environment) {
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
        ProxyFactory proxyFactory = new ProxyFactory();
        // proxyFactory.setOptimize(true);
        proxyFactory.setInterfaces(getObjectType(), SpringProxy.class);
        String host = restClientAnnotation.host();
        RestClientHostResolver defaultHostResolver = new DefaultRestClientHostResolver(EnvironmentPropUtils.tryFindRealVal(host, environment));
        RestClientSpiProviderValueObject restClientSpiProvider = buildSpiProvider();
        RestTemplate restTemplate = findRestTemplate(restClientSpiProvider);
        Map<Method, RestClientRequestMetaInfo> methodRestClientRequestMetaInfoMap = buildRequestMetaInfoMap(restClientInterface, restClientAnnotation);
        List<SimpleRestClient.OriginalRestClientMethodInfo> originalRestClientMethodInfos = new ArrayList<>();
        for (Method method : methodRestClientRequestMetaInfoMap.keySet()) {
            RestClientRequestMetaInfo restClientRequestMetaInfo = methodRestClientRequestMetaInfoMap.get(method);
            RequestInterceptor requestInterceptorInstance = findInstanceFromIoc(restClientRequestMetaInfo.getRequestInterceptor(), defaultRequestInterceptor);
            RestClientHostResolver restClientHostResolverInstance = findInstanceFromIoc(restClientRequestMetaInfo.getHostResolver(), defaultHostResolver);
            SimpleRestClient.OriginalRestClientMethodInfo originalRestClientMethodInfo =
                    new SimpleRestClient.OriginalRestClientMethodInfo(method, restClientRequestMetaInfo, requestInterceptorInstance, restClientHostResolverInstance);
            originalRestClientMethodInfos.add(originalRestClientMethodInfo);
        }
        SimpleRestClient simpleRestClient = new SimpleRestClient(restClientInterface, originalRestClientMethodInfos,
                restTemplate, restClientSpiProvider);
        proxyFactory.addAdvice(new RestClientMethodInterceptor(simpleRestClient));
        restClient = (T) proxyFactory.getProxy();
    }

    private <C extends R, R> R findInstanceFromIoc(Class<C> clz, R defaultInstance) {
        //如果没有设置具体的实现类作为interceptor，则不去spring容器中查找
        if (Modifier.isInterface(clz.getModifiers()) || Modifier.isAbstract(clz.getModifiers())) {
            return defaultInstance;
        } else {
            return applicationContext.getBean(clz);
        }
    }

    private Map<Method, RestClientRequestMetaInfo> buildRequestMetaInfoMap(Class<?> restClientInterface, RestClient restClientAnnotation) {
        Map<Method, RestClientRequestMetaInfo> result = new HashMap<>();
        Method[] declaredMethods = restClientInterface.getDeclaredMethods();
        String basePath = findBasePath(restClientAnnotation);

        @SuppressWarnings("rawtypes")
        Class<? extends ResponseTemplate> responseTemplateOfClz = restClientAnnotation.responseTemplate();
        Class<? extends RequestInterceptor> requestInterceptorOfClz = restClientAnnotation.requestInterceptor();
        for (Method declaredMethod : declaredMethods) {
            RestClientRequestMetaInfo requestMetaInfo = new RestClientRequestMetaInfo();
            requestMetaInfo.setResponseType(declaredMethod.getGenericReturnType());    //TODO 是否需要将基本数据类型转换成包装类型
            RestClientRequest restClientRequest = declaredMethod.getAnnotation(RestClientRequest.class);

            HttpMethod httpMethod;
            String path;
            String contentType;
            @SuppressWarnings("rawtypes") Class<? extends ResponseTemplate> responseTemplateType;
            Class<? extends RequestInterceptor> requestInterceptor;

            if (Objects.isNull(restClientRequest)) {
                path = basePath;
                //视为GET请求，其余配置全部使用类注解上的RestClient的配置
                httpMethod = HttpMethod.GET;
                contentType = restClientAnnotation.contentType();
                responseTemplateType = responseTemplateOfClz;
                requestInterceptor = requestInterceptorOfClz;
            } else {
                httpMethod = restClientRequest.method();
                contentType = restClientRequest.contentType();
                if (StringUtils.isBlank(contentType)) {
                    contentType = restClientAnnotation.contentType();
                }
                responseTemplateType = restClientRequest.responseTemplate();
                if (Modifier.isInterface(responseTemplateType.getModifiers()) && !Objects.equals(responseTemplateType, NonResponseTemplate.class)) {
                    responseTemplateType = responseTemplateOfClz;
                }
                //请求Interceptor
                requestInterceptor = restClientRequest.requestInterceptor();
                if (Modifier.isInterface(requestInterceptor.getModifiers())) {
                    requestInterceptor = requestInterceptorOfClz;
                }
                path = appendUrl(basePath, EnvironmentPropUtils.tryFindRealVal(restClientRequest.path(), environment));
            }
            requestMetaInfo.setPath(path);
            requestMetaInfo.setHostResolver(restClientAnnotation.hostResolver());
            requestMetaInfo.setHttpMethod(httpMethod);
            requestMetaInfo.setContentType(contentType);
            requestMetaInfo.setResponseTemplateType(responseTemplateType);
            requestMetaInfo.setRequestInterceptor(requestInterceptor);
            setRequestField(requestMetaInfo, declaredMethod);
            result.put(declaredMethod, requestMetaInfo);
        }
        return result;
    }

    private RestClientSpiProviderValueObject buildSpiProvider() {
        RestClientSpiProvider spiProviderFromAnnotation = this.restClientAnnotation.spiProvider();
        RestClientSpiProvider spiProviderFromClz = this.restClientInterface.getAnnotation(RestClientSpiProvider.class);
        RestClientSpiProvider spiProviderFromPackage = this.restClientInterface.getPackage().getAnnotation(RestClientSpiProvider.class);
        if (Objects.isNull(spiProviderFromClz) && Objects.isNull(spiProviderFromPackage)) {
            return RestClientSpiProviderValueObject.of(spiProviderFromAnnotation);
        } else if (Objects.isNull(spiProviderFromClz)) {
            return RestClientSpiProviderValueObject.preferFirst(spiProviderFromAnnotation, spiProviderFromPackage);
        } else if (Objects.isNull(spiProviderFromPackage)) {
            return RestClientSpiProviderValueObject.preferFirst(spiProviderFromAnnotation, spiProviderFromClz);
        } else {
            //三者都不为空
            RestClientSpiProviderValueObject second = RestClientSpiProviderValueObject.preferFirst(spiProviderFromClz, spiProviderFromPackage);
            return RestClientSpiProviderValueObject.preferFirst(spiProviderFromAnnotation, second);
        }
    }

    private void setRequestField(@Nonnull RestClientRequestMetaInfo requestMetaInfo, Method method) {
        Parameter[] parameters = method.getParameters();
        if (Objects.isNull(parameters) || parameters.length == 0) {
            return;
        }
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (Objects.isNull(parameterNames)) {
            parameterNames = Arrays.stream(parameters).map(Parameter::getName).toArray(String[]::new);
        }
        if (parameterNames.length != parameters.length) {
            throw new RuntimeException("parameterNames length should equal with parameters length");
        }
        for (int i = 0; i < parameters.length; i++) {
            RequestFieldValueObject requestFieldValueObject = new RequestFieldValueObject();
            Parameter parameter = parameters[i];
            String parameterName = parameterNames[i];
            requestFieldValueObject.setJavaType(parameter.getType());
            RequestField requestFieldAnnotation = parameter.getAnnotation(RequestField.class);
            @Nonnull RequestFieldType requestFieldType;
            if (Objects.isNull(requestFieldAnnotation)) {
                requestFieldType = RequestFieldType.AUTO;
                requestFieldValueObject.setName(parameterName);
                requestFieldValueObject.setJsonFormatAble(true);
            } else {
                //如果注解中指明的名称则使用注解中的名称，否则使用方法参数名称
                String name = Optional.of(requestFieldAnnotation.name()).filter(StringUtils::isNotBlank).orElse(parameterName);
                requestFieldValueObject.setName(name);
                requestFieldType = requestFieldAnnotation.type();
                requestFieldValueObject.setJsonFormatAble(requestFieldAnnotation.jsonFormatAble());
            }
            if (Objects.equals(requestFieldType, RequestFieldType.AUTO)) {
                HttpMethod httpMethod = requestMetaInfo.getHttpMethod();
                if (Objects.equals(httpMethod, HttpMethod.GET) || Objects.equals(httpMethod, HttpMethod.DELETE)) {
                    requestFieldValueObject.setType(RequestFieldType.QUERY);
                } else {
                    requestFieldValueObject.setType(RequestFieldType.BODY);
                }
            } else {
                requestFieldValueObject.setType(requestFieldType);
            }
            requestMetaInfo.addOrderedRequestField(requestFieldValueObject);
        }

    }

    private String findBasePath(@Nonnull RestClient restClientAnnotation) {
        String basePath = restClientAnnotation.path();
        return EnvironmentPropUtils.tryFindRealVal(basePath, environment);
    }

    private String appendUrl(String url, String append) {
        if (StringUtils.isBlank(append)) return url;
        if (!StringUtils.endsWith(url, URL_PATH_SEPARATOR) && !StringUtils.startsWith(append, URL_PATH_SEPARATOR)) {
            return url + URL_PATH_SEPARATOR + append;
        }
        return url + append;
    }

    private RestTemplate findRestTemplate(RestClientSpiProviderValueObject restClientSpiProvider) {
        String restTemplateBeanName = restClientAnnotation.restTemplateBeanName();
        String realRestTemplateBeanName = EnvironmentPropUtils.tryFindRealVal(restTemplateBeanName, environment);
        return StringUtils.isBlank(realRestTemplateBeanName) ? new DefaultRestTemplate(restClientSpiProvider, restClientInterface) : (RestTemplate) applicationContext.getBean(realRestTemplateBeanName);
    }

    private static class DefaultRequestInterceptor extends RemoveNullQueryRequestInterceptor implements RequestInterceptor {
    }
}
