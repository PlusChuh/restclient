package com.pluschuh.restclient.spi.provide;

import com.pluschuh.restclient.spi.ConfigOfDefaultRT;

/**
 * 默认的restTemplate的部分配置
 *
 * @author pluschuh
 */
public class SimpleConfigOfDefaultRT implements ConfigOfDefaultRT {

    @Override
    public long readTimeOut() {
        return 60;
    }

    @Override
    public long connectTimeout() {
        return 60;
    }
}
