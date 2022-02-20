package com.pluschuh.restclient.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 路径参数值对象
 *
 * @author pluschuh
 */
@Getter
@EqualsAndHashCode
public class PathVariableValueObject {

    private final String name;
    private final String value;

    public PathVariableValueObject(String name, String value) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
            throw new RuntimeException("PathVariable must not be blank");
        }
        this.name = name;
        this.value = value;
    }
}
