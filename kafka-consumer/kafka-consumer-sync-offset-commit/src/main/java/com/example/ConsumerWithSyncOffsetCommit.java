package com.example;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConsumerWithSyncOffsetCommit {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerWithSyncOffsetCommit.class);
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private final static String GROUP_ID = "test-group";

    public static void main(String[] args) {
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            // 개별 레코드 단위로 매번 오프셋을 커밋하고 싶은 경우
            Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();

            for (ConsumerRecord<String, String> record : records) {
                logger.info("record:{}", record);
                // 처리한 오프셋에 1을 더한 값을 커밋을 해야 이후 컨슈머가 poll()을 수행할 때 마지막으로 커밋한 오프셋부터 레코드를 리턴하기 때문이다.
                currentOffset.put(
                        new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset() + 1, null));
                consumer.commitSync(currentOffset);
            }
        }
    }
}