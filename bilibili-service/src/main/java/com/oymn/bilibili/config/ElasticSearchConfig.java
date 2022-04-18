package com.oymn.bilibili.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@Configuration
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {
    
    @Value("${elasticsearch.url}")
    private String esUrl;
    
    
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        //配置es的地址
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(esUrl)
                .build();
        
        //返回es的客户端
        return RestClients.create(clientConfiguration).rest();
    }
}
