package com.pluschuh.restclient.annotation;

import com.pluschuh.restclient.support.RequestInterceptor;
import com.pluschuh.restclient.support.ResponseTemplate;
import com.pluschuh.restclient.support.NonResponseTemplate;
import org.springframework.http.HttpMethod;

import java.lang.annotation.*;

/**
 * rest客户端请求
 *
 * @author pluschuh
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RestClientRequest {

    /**
     * 请求的基础路径，默认为空，支持${XXX}
     *
     * @return 请求的基础路径
     */
    String path() default "";

    /**
     * 请求类型，默认为GET
     *
     * @return HTTP请求类型
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * 请求内容类型，默认为空，当为空时则默认使用类上的注解@RestClient中的contentType作为类型
     *
     * @return 请求内容类型
     * @see RestClient#contentType()
     */
    String contentType() default "";

    /**
     * 响应模板类型，仅当设置为非抽象的实现类时有效，
     * 当该值不是某个具体的实现类时，则默认使用类上的注解@RestClient中的responseTemplate，
     * 当不想使用响应模板时可指定该值为NonResponseTemplate.class
     *
     * @return 响应模板类型
     * @see RestClient#responseTemplate()
     * @see NonResponseTemplate
     */
    Class<? extends ResponseTemplate> responseTemplate() default ResponseTemplate.class;

    /**
     * 请求拦截类型，仅当设置为非抽象的实现类时且该实现类已注入到spring容器中时有效，
     * 当该值不是某个具体的实现类时，则默认使用类上的注解@RestClient中的requestInterceptor
     *
     * @return 请求拦截类型
     * @see RestClient#requestInterceptor()
     */
    Class<? extends RequestInterceptor> requestInterceptor() default RequestInterceptor.class;
}
