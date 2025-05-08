package com.bits.borrowservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.borrow-events.name:borrow-events}")
    private String borrowEventsTopicName;

    @Value("${kafka.topic.borrow-events.partitions:3}")
    private int borrowEventsPartitions;

    @Value("${kafka.topic.borrow-events.replicas:1}")
    private int borrowEventsReplicas;

    @Value("${kafka.topic.return-events.name:return-events}")
    private String returnEventsTopicName;

    @Value("${kafka.topic.return-events.partitions:3}")
    private int returnEventsPartitions;

    @Value("${kafka.topic.return-events.replicas:1}")
    private int returnEventsReplicas;

    @Value("${kafka.topic.due-date-events.name:due-date-events}")
    private String dueDateEventsTopicName;

    @Value("${kafka.topic.due-date-events.partitions:3}")
    private int dueDateEventsPartitions;

    @Value("${kafka.topic.due-date-events.replicas:1}")
    private int dueDateEventsReplicas;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic borrowEventsTopic() {
        return TopicBuilder.name(borrowEventsTopicName)
                .partitions(borrowEventsPartitions)
                .replicas(borrowEventsReplicas)
                .build();
    }

    @Bean
    public NewTopic returnEventsTopic() {
        return TopicBuilder.name(returnEventsTopicName)
                .partitions(returnEventsPartitions)
                .replicas(returnEventsReplicas)
                .build();
    }

    @Bean
    public NewTopic dueDateEventsTopic() {
        return TopicBuilder.name(dueDateEventsTopicName)
                .partitions(dueDateEventsPartitions)
                .replicas(dueDateEventsReplicas)
                .build();
    }
} 