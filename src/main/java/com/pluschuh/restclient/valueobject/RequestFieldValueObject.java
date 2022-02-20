package com.pluschuh.restclient.valueobject;

import com.pluschuh.restclient.enums.RequestFieldType;
import lombok.Data;

/**
 * 请求字段值对象
 *
 * @author pluschuh
 */
@Data
public class RequestFieldValueObject {

    private String name;

    private RequestFieldType type;

    private Class<?> javaType;

    private boolean jsonFormatAble;

    public boolean isJsonFormatAble() {
        if (javaType.isPrimitive()
                || javaType.equals(Integer.class)
                || javaType.equals(Long.class)
                || javaType.equals(Character.class)
                || javaType.equals(Byte.class)
                || javaType.equals(Short.class)
                || javaType.equals(Boolean.class)
                || javaType.equals(Double.class)
                || javaType.equals(Float.class)
                || javaType.equals(String.class)) {
            return false;
        }
        return jsonFormatAble;
    }
}
