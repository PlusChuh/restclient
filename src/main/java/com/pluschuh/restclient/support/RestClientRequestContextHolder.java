package com.pluschuh.restclient.support;

public class RestClientRequestContextHolder {

    public static RestClientRequestContext get() {
        return SimpleRestClient.CURRENT_REQUEST_CONTEXT.get();
    }

}
