package com.apexobserve.ingestionservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean public NewTopic validatedMetricsTopic() { return TopicBuilder.name("telemetry.metrics.validated").partitions(3).replicas(1).build(); }
    @Bean public NewTopic validatedLogsTopic() { return TopicBuilder.name("telemetry.logs.validated").partitions(3).replicas(1).build(); }
    @Bean public NewTopic validatedTracesTopic() { return TopicBuilder.name("telemetry.traces.validated").partitions(3).replicas(1).build(); }
}
