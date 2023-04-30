package com.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

public class ProducerWithCustomPartitioner {
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";

    public static void main(String[] args) {

        /* 커스텀 파티셔너를 지정한 경우 ProducerConfig의 PARTITIONER_CLASS_CONFIG 옵션을 사용자 생성 파티셔너로 설정하여
        * KafkaProducer 인스턴스를 생성해야 한다.
        */
        Properties configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class);

        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "Pangyo", "Pangyo");
        /* KafkaProducer의 send() 메서도는 Future 객체를 반환한다.
        * 이 객체는 RecordMetadata의 비동기 결과를 표현하는 것으로 ProducerRecord가
        * 카프카 브로커에 정상적으로 적재되었는지에 대한 데이터가 포함되어 있다.
        */
        producer.send(record);
        producer.flush();
        producer.close();
    }
}