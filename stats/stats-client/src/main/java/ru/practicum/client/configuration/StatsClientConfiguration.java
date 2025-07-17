package ru.practicum.client.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class StatsClientConfiguration {

    @Value("${stats-server.url}")
    String baseURI;

    @Bean
    RestClient restClient(RestClient.Builder builder) {

        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(baseURI);
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return builder
                .uriBuilderFactory(defaultUriBuilderFactory)
                .build();
    }
}