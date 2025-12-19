package com.ocare.ocarebackend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic healthLogTopic() {
        return TopicBuilder.name("health-log-topic-v1")
                .partitions(1)
                .build();
    }

    @Bean
    public NewTopic healthLogDltTopic() {
        return TopicBuilder.name("health-log-topic-v1.DLT")
                .partitions(1)
                .build();
    }
}
