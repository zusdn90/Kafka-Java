package com.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerWorker implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerWorker.class);
    private Properties prop;
    private String topic;
    private String threadName;
    // KafkaConsumer 클래스는 스레드 세이프하지 않다. -> 스레드별로 KafkaConsumer 인스턴스를 별개로 만들어서 운영해야만 한다.
    private KafkaConsumer<String, String> consumer;

    ConsumerWorker(Properties prop, String topic, int number) {
        this.prop = prop;
        this.topic = topic;
        this.threadName = "consumer-thread-" + number;
    }

    @Override
    public void run() {
        consumer = new KafkaConsumer<>(prop);
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                logger.info("{}", record);
            }
            consumer.commitSync();
        }
    }
}