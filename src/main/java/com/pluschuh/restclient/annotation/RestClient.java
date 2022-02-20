package com.pluschuh.restclient.annotation;

import com.pluschuh.restclient.support.RequestInterceptor;
import com.pluschuh.restclient.support.ResponseTemplate;
import com.pluschuh.restclient.support.RestClientHostResolver;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

/**
 * rest客户端注解，仅当使用在接口上时有效，TODO 后续考虑支持使用在类上
 *
 * @author pluschuh
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RestClient {

    /**
     * 名称，会被用作spring容器中的bean名称，默认为类名首字母小写，支持${XXX}
     *
     * @return rest客户端名称
     */
    String name() default "";

    /**
     * 要请求的服务端的域名，默认为空，支持${XXX}
     *
     * @return 将要请求的服务端的域名
     */
    String host() default "";

    /**
     * 要使用的restTemplate在spring容器中的名称，支持${XXX}，默认为空即使用默认创建的restTemplate
     *
     * @return 要使用的restTemplate在spring容器中的名称
     */
    String restTemplateBeanName() default "";

    /**
     * 要请求的服务端的域名解析器，优先级大于直接指定的host()
     *
     * @return 将要请求的服务端的域名解析器类，仅当该类为实现类且已注册到spring容器时有效
     */
    Class<? extends RestClientHostResolver> hostResolver() default RestClientHostResolver.class;

    /**
     * 请求的基础路径，默认为空，支持${XXX}
     *
     * @return 请求的基础路径
     */
    String path() default "";

    /**
     * 请求内容类型，默认为JSON
     *
     * @return 请求内容类型
     */
    String contentType() default MediaType.APPLICATION_JSON_VALUE;

    /**
     * 仅当含有某个配置时才会自动生成rest客户端, 默认为空
     *
     * @return 用于判断是否自动生成rest客户端的配置条件
     */
    String conditionalOnProperty() default "";

    /**
     * 响应模板类型，仅当设置为非抽象的实现类时有效
     *
     * @return 响应模板类型
     */
    Class<? extends ResponseTemplate> responseTemplate() default ResponseTemplate.class;

    /**
     * 请求拦截类型，仅当设置为非抽象的实现类时且该实现类已注入到spring容器中时有效
     *
     * @return 请求拦截类型
     */
    Class<? extends RequestInterceptor> requestInterceptor() default RequestInterceptor.class;

    /**
     * spi实现者，其中的restTemplate相关配置仅当restTemplateBeanName为空时有效
     *
     * @return spi实现者
     * @see #restTemplateBeanName()
     * @see RestClientSpiProvider
     */
    RestClientSpiProvider spiProvider() default @RestClientSpiProvider;
}
