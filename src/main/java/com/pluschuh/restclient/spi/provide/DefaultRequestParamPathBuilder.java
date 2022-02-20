package com.pluschuh.restclient.spi.provide;

import com.pluschuh.restclient.spi.RequestParamPathBuilder;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 默认的请求参数路径构造接口
 *
 * @author pluschuh
 */
public class DefaultRequestParamPathBuilder implements RequestParamPathBuilder {


    public static final String EQUALS_MARK = "=";
    public static final String AMPERSAND_MARK = "&";
    public static final String QUESTION_MARK = "?";

    /**
     * 用于拼接相同key的请求参数值
     */
    private static final String LIST_PARAM_VAL_APPENDER = ",";

    @Override
    public String buildPathOfParams(@Nonnull Map<String, Object> requestParams) {
        if (MapUtils.isEmpty(requestParams)) {
            return StringUtils.EMPTY;
        }
        Map<String, Object> finalParams = new HashMap<>();
        for (String key : requestParams.keySet()) {
            Object value = requestParams.get(key);
            if (value instanceof List) {
                List<?> values = (List<?>) value;
                if (values.isEmpty()) continue;
                StringBuilder wholeValue = new StringBuilder();
                int lastIndex = values.size() - 1;
                for (int i = 0; i < lastIndex; i++) {
                    if (isRequestParamValueEmpty(values.get(i))) continue;
                    wholeValue.append(values.get(i)).append(LIST_PARAM_VAL_APPENDER);
                }
                if (!isRequestParamValueEmpty(values.get(lastIndex))) {
                    wholeValue.append(values.get(lastIndex));
                }
                finalParams.put(key, new String(wholeValue));
            } else if (!isRequestParamValueEmpty(value)) {
                finalParams.put(key, value);
            }
        }

        String paramsStr = finalParams.entrySet()
                .stream()
                .filter(entry -> Objects.nonNull(entry) && Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
                .map(entry -> entry.getKey() + EQUALS_MARK + entry.getValue())
                .collect(Collectors.joining(AMPERSAND_MARK));

        return QUESTION_MARK + paramsStr;
    }

    /**
     * 判断请求参数的值是否为null或空字符串
     *
     * @param paramValue 请求参数的值
     * @return 是否为null或空字符串
     */
    private boolean isRequestParamValueEmpty(Object paramValue) {
        return Objects.isNull(paramValue)
                || StringUtils.isEmpty(paramValue.toString());
    }

}
