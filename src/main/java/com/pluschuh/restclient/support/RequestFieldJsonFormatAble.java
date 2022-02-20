package com.pluschuh.restclient.support;

import com.pluschuh.restclient.spi.RequestObjectConverter;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 请求字段对象是否可以转json标记接口，
 * 实现了该接口的请求对象，不会被请求对象转换器强转，而是直接调用自身的asMap方法
 *
 * @author pluschuh
 */
public interface RequestFieldJsonFormatAble {

    /**
     * 如果某个请求对象实现了该接口，会使用该方法的返回作为后续逻辑的前置条件。
     * 如果某个请求对象没有实现该接口，则会直接使用converter对象进行强转
     *
     * @param converter 请求对象转换器
     * @return 键值对，key应当为请求参数或请求体的key，value则对应其值。默认实现与converter强转保持一致
     */
    default Map<String, Object> asMap(@Nonnull RequestObjectConverter converter) {
        return converter.convert(this);
    }

}
