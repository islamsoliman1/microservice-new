package com.programming.techie.docs;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DocumentationConfig {
    @Bean("openApiWebClient")
    @LoadBalanced
    public WebClient.Builder openApiWebClient() {
        return WebClient.builder().codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024));
    }
}
