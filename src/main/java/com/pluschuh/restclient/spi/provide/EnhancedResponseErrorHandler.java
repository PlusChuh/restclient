package com.pluschuh.restclient.spi.provide;

import com.pluschuh.restclient.spi.ResponseErrorHandlerOfDefaultRT;
import com.pluschuh.restclient.support.RestClientRequestContext;
import com.pluschuh.restclient.support.RestClientRequestContextHolder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * 增加的错误处理器
 */
public class EnhancedResponseErrorHandler extends SimpleResponseErrorHandler implements ResponseErrorHandlerOfDefaultRT {

    /**
     * 类内部的spring容器中的服务，spring容器启动后，由组件通过反射自动注入
     */
    private static EnhancedResponseErrorHandlerService innerSpringService = null;

    private static void setInnerSpringService(EnhancedResponseErrorHandlerService innerSpringService) {
        EnhancedResponseErrorHandler.innerSpringService = innerSpringService;
    }

    @Override
    public final void handleError(@Nonnull URI url, @Nonnull HttpMethod method, @Nonnull ClientHttpResponse response) throws IOException {
        boolean errorHandled = false;
        if (Objects.nonNull(innerSpringService)) {
            errorHandled = innerSpringService.handleError(url, method, response);
        }
        ERROR_HANDLED_FLAG.set(errorHandled);
        super.handleError(url, method, response);
    }

    public static class EnhancedResponseErrorHandlerService {

        private final List<Receiver> errorHandlers;

        public EnhancedResponseErrorHandlerService(List<Receiver> errorHandlers) {
            this.errorHandlers = errorHandlers;
        }

        public final boolean handleError(@Nonnull URI url, @Nonnull HttpMethod method, @Nonnull ClientHttpResponse response) {
            if (Objects.isNull(errorHandlers)) return false;
            for (Receiver errorHandler : errorHandlers) {
                if (Objects.nonNull(errorHandler) && errorHandler.requestToHandle().compare(url, method) && errorHandler.shouldHandle(RestClientRequestContextHolder.get())) {
                    return errorHandler.handleError(url, method, response, RestClientRequestContextHolder.get());
                }
            }
            return false;
        }

    }

    public interface Receiver {

        /**
         * 要处理的请求
         *
         * @return 要处理的请求对象
         */
        @Nonnull
        RequestToHandle requestToHandle();

        /**
         * 是否需要处理。在对比requestToHandle与当前请求一致后调用该方法，默认返回true。
         * 该方法适用于相同请求路径、相同请求方式、但不同请求参数/请求体会有不同响应体结构的场景。
         *
         * @param restClientRequestContext rest客户端请求上下文，正常情况下不为空
         * @return true-需要处理，false-不需要处理。
         */
        default boolean shouldHandle(RestClientRequestContext restClientRequestContext) {
            return true;
        }

        /**
         * @param url                      请求url
         * @param method                   请求方式
         * @param response                 响应
         * @param restClientRequestContext rest客户端请求上下文，正常情况下不为空
         * @return 是否已处理错误响应，true-已处理，false-未处理或需要调用方抛出异常
         */
        boolean handleError(@Nonnull URI url, @Nonnull HttpMethod method, @Nonnull ClientHttpResponse response, @Nonnull RestClientRequestContext restClientRequestContext);

    }

    @Data
    public static class RequestToHandle {
        private static final String IGNORE_HOST = "IGNORED";
        private static final String SLASH = "/";
        private static final String OPEN_BRACE = "{";
        private static final String CLOSE_BRACE = "}";

        private final String host;
        private final boolean hostIgnored;
        private final String path;
        private final boolean dynamicPath;
        private final HttpMethod httpMethod;

        private RequestToHandle(String host, String path, HttpMethod httpMethod) {
            if (StringUtils.isBlank(path)) {
                throw new RuntimeException("path must not be blank");
            }
            if (Objects.isNull(httpMethod)) {
                throw new RuntimeException("httpMethod must not be null");
            }
            this.host = StringUtils.isBlank(host) ? IGNORE_HOST : host;
            this.hostIgnored = StringUtils.equals(this.host, IGNORE_HOST);
            this.path = formatPath(path);
            this.dynamicPath = StringUtils.contains(this.path, OPEN_BRACE) && StringUtils.contains(this.path, CLOSE_BRACE);
            this.httpMethod = httpMethod;
        }

        public static RequestToHandle of(String path, HttpMethod httpMethod) {
            return of(path, httpMethod, IGNORE_HOST);
        }

        public static RequestToHandle of(String path, HttpMethod httpMethod, String host) {
            return new RequestToHandle(host, path, httpMethod);
        }

        private String formatPath(String originalPath) {
            if (StringUtils.endsWith(originalPath, SLASH)) {
                originalPath = originalPath.substring(0, originalPath.length() - 1);
            }
            StringBuilder ret = new StringBuilder();
            if (!StringUtils.startsWith(originalPath, SLASH)) {
                ret.append(SLASH);
            }
            ret.append(originalPath);
            return ret.toString();
        }

        public boolean compare(@Nonnull URI url, @Nonnull HttpMethod method) {
            boolean same = Objects.equals(method, this.getHttpMethod()) && pathCompare(url.getPath());
            if (!isHostIgnored()) {
                same = same && Objects.equals(url.getHost(), this.getHost());
            }
            return same;
        }

        private boolean pathCompare(String realUrlPath) {
            if (!dynamicPath) {
                return Objects.equals(realUrlPath, this.getPath());
            }
            return compareDynamicPath(realUrlPath, this.getPath());
        }

        /**
         * 对比实际调用的key和动态请求路径Map中的key是否能对应上
         *
         * @param realUrlPath    实际调用的请求路径, 如/api/v1/product/1
         * @param dynamicUrlPath 动态请求路径Map中的key, 如/api/v1/product/{productId}
         * @return 能对应上则返回true, 否则返回false, 如/api/v1/product/1与/api/v1/product/{productId}能够对应上, 返回true
         */
        private boolean compareDynamicPath(String realUrlPath, String dynamicUrlPath) {
            if (StringUtils.isBlank(realUrlPath) || StringUtils.isBlank(dynamicUrlPath)) return false;
            String[] partsOfRealUrlPath = realUrlPath.split(SLASH);
            String[] partsOfDynamicUrlPath = dynamicUrlPath.split(SLASH);
            if (partsOfDynamicUrlPath.length != partsOfRealUrlPath.length) return false;
            for (int i = 0; i < partsOfDynamicUrlPath.length; i++) {
                if (partsOfDynamicUrlPath[i].startsWith(OPEN_BRACE) && partsOfDynamicUrlPath[i].endsWith(CLOSE_BRACE))
                    continue;
                if (!partsOfDynamicUrlPath[i].equals(partsOfRealUrlPath[i])) return false;
            }
            return true;
        }

    }

}
