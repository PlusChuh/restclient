package com.pluschuh.restclient.spi.provide;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluschuh.restclient.spi.RequestBodySerializer;
import com.pluschuh.restclient.valueobject.RestClientRequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 默认的请求体序列化器spi接口实现
 *
 * @author pluschuh
 */
public class DefaultRequestBodySerializer implements RequestBodySerializer {

    private final Map<String, TypedSerializer> serializerMap;

    public DefaultRequestBodySerializer() {
        serializerMap = new HashMap<>();
        init();
    }

    private void init() {
        addSerializer(new JsonTypeSerializer());
        addSerializer(new UrlencodedTypeSerializer());
        addSerializer(new FormDataTypeSerializer());
    }

    @Override
    public Object serialize(RestClientRequestTemplate requestTemplate) {
        String contentType = findContentType(requestTemplate);
        TypedSerializer typedSerializer = serializerMap.get(contentType);
        return Objects.isNull(typedSerializer) ? requestTemplate.getRequestBody() : typedSerializer.serialize(requestTemplate);

    }

    protected void addSerializer(TypedSerializer typedSerializer) {
        if (Objects.nonNull(typedSerializer) && StringUtils.isNotBlank(typedSerializer.contentType())) {
            serializerMap.put(typedSerializer.contentType(), typedSerializer);
        }
    }

    protected void removeSerializer(String contentType) {
        serializerMap.remove(contentType);
    }

    private String findContentType(RestClientRequestTemplate requestTemplate) {
        String contentType = null;
        Object contentTypeObj = requestTemplate.getRequestHeader().get(HttpHeaders.CONTENT_TYPE);
        if (Objects.nonNull(contentTypeObj)) {
            contentType = String.valueOf(contentTypeObj);
        }
        if (StringUtils.isBlank(contentType)) {
            contentType = MediaType.APPLICATION_JSON_VALUE;
        }
        return contentType;
    }

    public interface TypedSerializer {

        @Nonnull
        String contentType();

        Object serialize(Map<String, Object> originalRequestBody);

        default Object serialize(RestClientRequestTemplate requestTemplate) {
            return serialize(requestTemplate.getRequestBody());
        }
    }

    protected static class JsonTypeSerializer implements TypedSerializer {

        private static final Logger LOGGER = LoggerFactory.getLogger(JsonTypeSerializer.class);

        private final static ObjectMapper MAPPER = new ObjectMapper();

        @Nonnull
        @Override
        public String contentType() {
            return MediaType.APPLICATION_JSON_VALUE;
        }

        @Override
        public Object serialize(Map<String, Object> originalRequestBody) {
            try {
                return MAPPER.writeValueAsString(originalRequestBody);
            } catch (JsonProcessingException e) {
                LOGGER.error("parse json string error, will return originalRequestBody", e);
                return originalRequestBody;
            }
        }
    }

    protected static class UrlencodedTypeSerializer implements TypedSerializer {

        @Nonnull
        @Override
        public String contentType() {
            return MediaType.APPLICATION_FORM_URLENCODED_VALUE;
        }

        @Override
        public Object serialize(Map<String, Object> originalRequestBody) {
            return originalRequestBody
                    .entrySet()
                    .stream()
                    .filter(entry -> Objects.nonNull(entry) && Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
                    .map(entry -> entry.getKey().trim() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
        }
    }

    protected static class FormDataTypeSerializer implements TypedSerializer {

        @Nonnull
        @Override
        public String contentType() {
            return MediaType.MULTIPART_FORM_DATA_VALUE;
        }

        @Override
        public Object serialize(Map<String, Object> originalRequestBody) {
            MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            originalRequestBody.forEach(multiValueMap::set);
            return multiValueMap;
        }
    }
}
