package com.pluschuh.restclient.spi.provide;

import com.pluschuh.restclient.spi.UriTemplateHandlerOfDefaultRT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 简单URI模板处理器，
 * 只对传入的uriTemplate做处理，会忽略uriVariables。
 *
 * @author pluschuh
 */
public class SimpleUriTemplateHandler implements UriTemplateHandlerOfDefaultRT {

    private final Logger LOGGER;

    private static final String EQUALS_MARK = "=";
    private static final String AMPERSAND_MARK = "&";
    private static final String QUESTION_MARK = "?";

    public SimpleUriTemplateHandler() {
        LOGGER = LoggerFactory.getLogger(getClass());
    }

    public SimpleUriTemplateHandler(@NonNull Class<?> loggerClz) {
        LOGGER = LoggerFactory.getLogger(loggerClz);
    }

    @NonNull
    @Override
    public URI expand(@NonNull String uriTemplate, @NonNull Map<String, ?> uriVariables) {
        //ignore uriVariables
        URI uri = URI.create(formatUriTemplate(uriTemplate));
        LOGGER.debug("the final request uri is {}", uri);
        return uri;
    }

    @NonNull
    @Override
    public URI expand(@NonNull String uriTemplate, @NonNull Object... uriVariables) {
        //ignore uriVariables
        URI uri = URI.create(formatUriTemplate(uriTemplate));
        LOGGER.debug("the final request uri is {}", uri);
        return uri;
    }

    /**
     * 格式化uri路径，主要用于将请求路径中的特殊字符进行转换
     *
     * @param uriTemplate uri路径
     * @return 格式化后的路径
     */
    private String formatUriTemplate(String uriTemplate) {
        //以?分割
        int paramStart = uriTemplate.indexOf(QUESTION_MARK);
        if (paramStart < 0 || paramStart + 1 == uriTemplate.length()) return uriTemplate;

        StringBuilder paramsBuilder = new StringBuilder();
        String hostWithPath = uriTemplate.substring(0, paramStart + 1);

        //以=分割
        String allParams = uriTemplate.substring(paramStart + 1);
        //app_id=ide_app&request_id=1625454011&sign=a76d83a52966f1167c89fc329682154b
        String[] paramsArr = allParams.split(AMPERSAND_MARK);
        for (String singleParamNameWithValue : paramsArr) {
            //app_id=ide_app
            int firstEqual = singleParamNameWithValue.indexOf(EQUALS_MARK);
            if (firstEqual < 0 || firstEqual + 1 == singleParamNameWithValue.length()) {
                paramsBuilder.append(singleParamNameWithValue).append(AMPERSAND_MARK);
                continue;
            }
            String paramName = singleParamNameWithValue.substring(0, firstEqual + 1);
            String paramValue = formatUrlValue(singleParamNameWithValue.substring(firstEqual + 1));
            paramsBuilder.append(paramName).append(paramValue).append(AMPERSAND_MARK);
        }
        return hostWithPath + paramsBuilder.substring(0, paramsBuilder.length() - 1);
    }

    private String formatUrlValue(String originalValue) {
        String result = originalValue;
        try {
            result = URLEncoder.encode(originalValue, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("formatUrlValue:: hit exception when format url value, will return original value {}", originalValue, e);
        }
        return result;
    }

}
