package com.pluschuh.restclient.valueobject;

import com.pluschuh.restclient.annotation.RestClientSpiProvider;
import com.pluschuh.restclient.spi.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * spi提供者注解值对象
 *
 * @author pluschuh
 */
@AllArgsConstructor
@EqualsAndHashCode
public class RestClientSpiProviderValueObject {

    private static final RestClientSpiProvider defaultRestClientSpiProvider;

    static {
        defaultRestClientSpiProvider = DefaultInfo.class.getAnnotation(RestClientSpiProvider.class);
    }

    private final Class<? extends RequestParamPathBuilder> requestParamPathBuilder;
    private final Class<? extends RequestBodySerializer> requestBodySerializer;
    private final Class<? extends RequestObjectConverter> requestObjectConvert;
    private final Class<? extends ConfigOfDefaultRT> configOfDefaultRT;
    private final Class<? extends ResponseErrorHandlerOfDefaultRT> responseErrorHandlerOfDefaultRT;
    private final Class<? extends UriTemplateHandlerOfDefaultRT> uriTemplateHandlerOfDefaultRT;

    public static RestClientSpiProviderValueObject of(RestClientSpiProvider restClientSpiProvider) {
        Assert.notNull(restClientSpiProvider, "restClientSpiProvider can not be null");
        return new RestClientSpiProviderValueObject(restClientSpiProvider.requestParamPathBuilder(),
                restClientSpiProvider.requestBodySerializer(),
                restClientSpiProvider.requestObjectConvert(),
                restClientSpiProvider.configOfDefaultRT(),
                restClientSpiProvider.responseErrorHandlerOfDefaultRT(),
                restClientSpiProvider.uriTemplateHandlerOfDefaultRT());
    }

    public static RestClientSpiProviderValueObject preferFirst(@Nonnull RestClientSpiProvider first, @Nonnull RestClientSpiProvider second) {
        return preferFirst(of(first), of(second));
    }

    public static RestClientSpiProviderValueObject preferFirst(@Nonnull RestClientSpiProviderValueObject first, @Nonnull RestClientSpiProvider second) {
        return preferFirst(first, of(second));
    }

    public static RestClientSpiProviderValueObject preferFirst(@Nonnull RestClientSpiProvider first, @Nonnull RestClientSpiProviderValueObject second) {
        return preferFirst(of(first), second);
    }

    public static RestClientSpiProviderValueObject preferFirst(@Nonnull RestClientSpiProviderValueObject first, @Nonnull RestClientSpiProviderValueObject second) {
        if (Objects.equals(first, second)) return first;
        //当第一个传入的值为默认值时，才取第二个的值
        return new RestClientSpiProviderValueObject(
                preferFirstClz(first.requestParamPathBuilder(), defaultInfo().requestParamPathBuilder(), second.requestParamPathBuilder()),
                preferFirstClz(first.requestBodySerializer(), defaultInfo().requestBodySerializer(), second.requestBodySerializer()),
                preferFirstClz(first.requestObjectConvert(), defaultInfo().requestObjectConvert(), second.requestObjectConvert()),
                preferFirstClz(first.configOfDefaultRT(), defaultInfo().configOfDefaultRT(), second.configOfDefaultRT()),
                preferFirstClz(first.responseErrorHandlerOfDefaultRT(), defaultInfo().responseErrorHandlerOfDefaultRT(), second.responseErrorHandlerOfDefaultRT()),
                preferFirstClz(first.uriTemplateHandlerOfDefaultRT(), defaultInfo().uriTemplateHandlerOfDefaultRT(), second.uriTemplateHandlerOfDefaultRT())
        );
    }

    private static <T> Class<? extends T> preferFirstClz(Class<? extends T> first, Class<? extends T> DEFAULT, Class<? extends T> second) {
        return Objects.equals(first, DEFAULT) ? second : first;
    }

    public Class<? extends RequestParamPathBuilder> requestParamPathBuilder() {
        return Objects.isNull(this.requestBodySerializer) ? defaultInfo().requestParamPathBuilder() : this.requestParamPathBuilder;
    }

    public Class<? extends RequestBodySerializer> requestBodySerializer() {
        return Objects.isNull(this.requestBodySerializer) ? defaultInfo().requestBodySerializer() : this.requestBodySerializer;
    }

    public Class<? extends RequestObjectConverter> requestObjectConvert() {
        return Objects.isNull(this.requestObjectConvert) ? defaultInfo().requestObjectConvert() : this.requestObjectConvert;
    }

    public Class<? extends ConfigOfDefaultRT> configOfDefaultRT() {
        return Objects.isNull(this.configOfDefaultRT) ? defaultInfo().configOfDefaultRT() : this.configOfDefaultRT;
    }

    public Class<? extends ResponseErrorHandlerOfDefaultRT> responseErrorHandlerOfDefaultRT() {
        return Objects.isNull(this.responseErrorHandlerOfDefaultRT) ? defaultInfo().responseErrorHandlerOfDefaultRT() : this.responseErrorHandlerOfDefaultRT;
    }

    public Class<? extends UriTemplateHandlerOfDefaultRT> uriTemplateHandlerOfDefaultRT() {
        return Objects.isNull(this.uriTemplateHandlerOfDefaultRT) ? defaultInfo().uriTemplateHandlerOfDefaultRT() : this.uriTemplateHandlerOfDefaultRT;
    }

    public static RestClientSpiProvider defaultInfo() {
        return defaultRestClientSpiProvider;
    }

    @RestClientSpiProvider
    private static class DefaultInfo {

    }

}
