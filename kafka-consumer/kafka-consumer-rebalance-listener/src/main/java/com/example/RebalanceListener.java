package com.example;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RebalanceListener implements ConsumerRebalanceListener {
    private final static Logger logger = LoggerFactory.getLogger(RebalanceListener.class);

    // 리밸런스가 끝난 뒤에 파티션이 할당 완료되면 호출된다.
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        logger.warn("Partitions are assigned : " + partitions.toString());

    }

    // 리밸런스가 시작되기 직전에 호출되는 메서드.
    // 마지막으로 처리한 레코드를 기준으로 커밋을 하기 위해서는 리밸런스 시작 직전에 커밋을 하면 되므로
    // onPartitionsRevoked() 메서드에 커밋을 구현하여 처리할 수 있다.
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        logger.warn("Partitions are revoked : " + partitions.toString());
        consumer.commitSync(currentOffset);
    }
}
