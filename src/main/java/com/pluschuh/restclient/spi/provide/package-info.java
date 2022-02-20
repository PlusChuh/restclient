/**
 * 该包中提供了一些spi接口的默认实现，
 * 当使用方未在META-INF.services目录下设置自己的实现时，系统会加载默认实现，
 * 使用@RestClientSpiProvider指定具体需要加载的实现类
 *
 * @see com.pluschuh.restclient.annotation.RestClientSpiProvider
 */
package com.pluschuh.restclient.spi.provide;