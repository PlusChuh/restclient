package com.pluschuh.restclient.annotation;

import com.pluschuh.restclient.spi.*;
import com.pluschuh.restclient.spi.provide.*;

import java.lang.annotation.*;

/**
 * spi提供者注解，当创建默认的restTemplate时与SimpleRestClient时，会根据此配置取创建。
 * 该注解可以用在包、类、或@RestClient注解中，
 * 当某个接口类上被@RestClient注解时，同时该接口类又被@RestClientSpiProvider注解，同时该接口类所在的包又被@RestClientSpiProvider注解，
 * 此时优先级从高到低为：接口类上被@RestClient注解中的@RestClientSpiProvider注解、接口类上的@RestClientSpiProvider注解、包上的@RestClientSpiProvider注解，
 * 取值时按照优先级顺序获取第一个非default的配置
 *
 * @author pluschuh
 */
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestClientSpiProvider {

    /**
     * 请求体序列化器，仅当需要变更时指定，
     * 仅当在META-INF/services目录下设置了相关实现时才会加载成功，否则会加载默认实现
     *
     * @return 请求体序列化器
     */
    Class<? extends RequestBodySerializer> requestBodySerializer() default DefaultRequestBodySerializer.class;

    /**
     * 请求参数路径构造器，仅当需要变更时指定，
     * 仅当在META-INF/services目录下设置了相关实现时才会加载成功，否则会加载默认实现
     *
     * @return 请求参数路径构造器
     */
    Class<? extends RequestParamPathBuilder> requestParamPathBuilder() default DefaultRequestParamPathBuilder.class;

    /**
     * 请求对象转换器，仅当需要变更时指定，
     * 仅当在META-INF/services目录下设置了相关实现时才会加载成功，否则会加载默认实现
     *
     * @return 请求对象转换器
     */
    Class<? extends RequestObjectConverter> requestObjectConvert() default DefaultRequestObjectConverter.class;

    /**
     * 默认的restTemplate的部分配置，仅当使用系统默认提供的RestTemplate时有效，
     * （当在@RestClient中指定了restTemplateBeanName时会取容器中查找对应的restTemplate，故此时该配置无作用）。
     * 且仅当在META-INF/services目录下设置了相关实现时才会加载成功，否则会加载默认实现
     *
     * @return 默认的restTemplate的部分配置
     * @see RestClient#restTemplateBeanName()
     */
    Class<? extends ConfigOfDefaultRT> configOfDefaultRT() default SimpleConfigOfDefaultRT.class;

    /**
     * 默认的restTemplate的响应错误处理器接口，仅当使用系统默认提供的RestTemplate时有效，
     * （当在@RestClient中指定了restTemplateBeanName时会取容器中查找对应的restTemplate，故此时该配置无作用）。
     * 且仅当在META-INF/services目录下设置了相关实现时才会加载成功，否则会加载默认实现
     *
     * @return 默认的restTemplate的部分配置
     * @see RestClient#restTemplateBeanName()
     */
    Class<? extends ResponseErrorHandlerOfDefaultRT> responseErrorHandlerOfDefaultRT() default SimpleResponseErrorHandler.class;

    /**
     * 默认的restTemplate的路径模板处理器接口，仅当使用系统默认提供的RestTemplate时有效，
     * （当在@RestClient中指定了restTemplateBeanName时会取容器中查找对应的restTemplate，故此时该配置无作用）。
     * 且仅当在META-INF/services目录下设置了相关实现时才会加载成功，否则会加载默认实现
     *
     * @return 默认的restTemplate的部分配置
     * @see RestClient#restTemplateBeanName()
     */
    Class<? extends UriTemplateHandlerOfDefaultRT> uriTemplateHandlerOfDefaultRT() default SimpleUriTemplateHandler.class;


    /**
     * 仅当在包级别设置了某个属性之后，需要在类级别重置为默认值时使用，
     * （实际上可以直接在类级别的注解中使用接口类类型或者任意未在META-INF下定义的实现类即可实现重置默认值的操作）
     */
    interface DefaultReset {
        Class<? extends RequestBodySerializer> requestBodySerializer = RequestBodySerializer.class;
        Class<? extends RequestObjectConverter> requestObjectConvert = RequestObjectConverter.class;
        Class<? extends ConfigOfDefaultRT> configOfDefaultRT = ConfigOfDefaultRT.class;
        Class<? extends ResponseErrorHandlerOfDefaultRT> responseErrorHandlerOfDefaultRT = ResponseErrorHandlerOfDefaultRT.class;
        Class<? extends UriTemplateHandlerOfDefaultRT> uriTemplateHandlerOfDefaultRT = UriTemplateHandlerOfDefaultRT.class;
    }
}
