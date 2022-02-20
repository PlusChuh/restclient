package com.pluschuh.restclient.spi;

import java.util.concurrent.TimeUnit;

/**
 * 默认的restTemplate的部分配置     TODO 后续可能会增加
 *
 * @author pluschuh
 */
public interface ConfigOfDefaultRT {

    long readTimeOut();

    default TimeUnit readTimeOutUnit() {
        return TimeUnit.SECONDS;
    }

    long connectTimeout();

    default TimeUnit connectTimeoutUnit() {
        return TimeUnit.SECONDS;
    }


}
