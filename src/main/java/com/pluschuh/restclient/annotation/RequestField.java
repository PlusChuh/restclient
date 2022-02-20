package com.pluschuh.restclient.annotation;

import com.pluschuh.restclient.enums.RequestFieldType;

import java.lang.annotation.*;

/**
 * 请求字段
 *
 * @author pluschuh
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RequestField {

    /**
     * 字段名称，必填，可用于注解在基本数据类型或String类型或不需要转json格式的字段上。
     * 仅当jsonFormatAble=false时有效。
     *
     * @return 请求字段名称，若请求类型为可转换成JSON对象的类型，则可默认填入空字符串
     */
    String name();

    /**
     * 请求字段类型，默认根据请求HTTP METHOD字段推测类型
     *
     * @return 请求字段类型
     */
    RequestFieldType type() default RequestFieldType.AUTO;

    /**
     * 该请求字段对象是否可转换成json格式，默认为true，但当检测到数据类型为基本数据类型及其包装类或String时会自动重置为false
     */
    boolean jsonFormatAble() default true;
}
