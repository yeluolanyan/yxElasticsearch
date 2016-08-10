package com.springapp.util;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by xinhuan on 2016/1/18.
 */
@Configuration
public class ConfigurationUtil {

    public @Bean JestClient jestClient() {
        String connectionUrl = "http://localhost:9200";
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(connectionUrl)
                .multiThreaded(true)
                .build());
        JestClient client = factory.getObject();
        return client;
    }
}
