package com.example;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import java.util.HashMap;
import java.util.Map;

// KafkaTemplate 빈 객체를 등록하기 위해 Configuration 어노테이션을 사용한다.
@Configuration
public class KafkaTemplateConfiguration {

    // 프로듀서 팩토리를 통해 만든 카프카 템플릿 객체를 빈으로 등록하여 사용한다.
    @Bean
    public KafkaTemplate<String, String> customKafkaTemplate() {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-kafka:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(props);

        return new KafkaTemplate<>(pf);
    }
}