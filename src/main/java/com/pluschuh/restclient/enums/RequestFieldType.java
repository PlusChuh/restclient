package com.pluschuh.restclient.enums;

/**
 * 请求参数字段类型
 *
 * @author pluschuh
 */
public enum RequestFieldType {

    /**
     * 路径参数类型
     */
    PATH_VARIABLE,

    /**
     * 查询参数类型，常用于GET和DELETE请求，PATCH等请求中也可使用
     */
    QUERY,

    /**
     * 请求体类型，除GET与DELETE外，默认均视为请求体类型
     */
    BODY,

    /**
     * 自动推测，当为GET或DELETE请求时，视为QUERY，否则视为BODY
     */
    AUTO,

    /**
     * 请求头参数
     */
    HEADER,

}
