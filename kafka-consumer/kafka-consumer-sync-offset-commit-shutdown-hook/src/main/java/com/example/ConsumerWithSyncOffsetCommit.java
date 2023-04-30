package com.example;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerWithSyncOffsetCommit {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerWithSyncOffsetCommit.class);
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private final static String GROUP_ID = "test-group";
    private static KafkaConsumer<String, String> consumer;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        consumer = new KafkaConsumer<>(configs);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("{}", record);
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            logger.warn("Wakeup consumer");
            // 리소스 종료 처리
        } finally {
            logger.warn("Consumer close");
            consumer.close();
        }
    }

    // shutdown hook은 사용자 또는 운영체제로부터 종료 요청을 받으면 실행하는 스레드를 뜻한다.
    static class ShutdownThread extends Thread {
        public void run() {
            logger.info("Shutdown hook");
            consumer.wakeup();
        }
    }
}