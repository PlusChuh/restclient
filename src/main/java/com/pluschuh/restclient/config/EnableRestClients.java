package com.pluschuh.restclient.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启Rest客户端注解
 *
 * @author pluschuh
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RestClientBeanDefinitionRegistrar.class, RestClientServices.class})
public @interface EnableRestClients {

    /**
     * 扫描路径，默认为使用该注解的类所在的包目录    TODO 后续考虑支持过滤
     *
     * @return 扫描路径
     */
    String[] scannedPackages() default {};

}
