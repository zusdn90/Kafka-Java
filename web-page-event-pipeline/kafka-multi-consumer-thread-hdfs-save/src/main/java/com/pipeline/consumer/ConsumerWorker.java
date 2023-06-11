package com.pipeline.consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ConsumerWorker.class);
    // 컨슈머의 poll() 메서드를 통해 전달받은 데이터를 임시 저장하는 버퍼 -> 파티션 번호와 메시지 값들이 저장된다.
    private static Map<Integer, List<String>> bufferString = new ConcurrentHashMap<>();
    // 오프셋 값을 저장하고 파일 이름을 저장할 때 오프셋 번호를 붙이는 데에 사용한다.
    private static Map<Integer, Long> currentFileOffset = new ConcurrentHashMap<>();

    private final static int FLUSH_RECORD_COUNT = 10;
    private Properties prop;
    private String topic;
    private String threadName;
    private KafkaConsumer<String, String> consumer;

    public ConsumerWorker(Properties prop, String topic, int number) {
        logger.info("Generate ConsumerWorker");
        this.prop = prop;
        this.topic = topic;
        this.threadName = "consumer-thread-" + number;
    }

    // 1. poll() 메소드 호출
    @Override
    public void run() {
        Thread.currentThread().setName(threadName);
        consumer = new KafkaConsumer<>(prop);
        consumer.subscribe(Arrays.asList(topic));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, String> record : records) {
                    addHdfsFileBuffer(record);
                }
                // 버퍼에 쌓인 데이터가 일정 개수 이상 쌓였을 경우 HDFS에 저장하는 로직을 수행
                saveBufferToHdfsFile(consumer.assignment());
            }
        } catch (WakeupException e) {
            logger.warn("Wakeup consumer");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            consumer.close();
        }
    }

    // 2. HDFS 적재 로직
    private void addHdfsFileBuffer(ConsumerRecord<String, String> record) {
        List<String> buffer = bufferString.getOrDefault(record.partition(), new ArrayList<>());
        buffer.add(record.value());
        bufferString.put(record.partition(), buffer);

        if (buffer.size() == 1)
            currentFileOffset.put(record.partition(), record.offset());
    }

    private void saveBufferToHdfsFile(Set<TopicPartition> partitions) {
        partitions.forEach(p -> checkFlushCount(p.partition()));
    }

    private void checkFlushCount(int partitionNo) {
        if (bufferString.get(partitionNo) != null) {
            if (bufferString.get(partitionNo).size() > FLUSH_RECORD_COUNT - 1) {
                save(partitionNo);
            }
        }
    }

    private void save(int partitionNo) {
        if (bufferString.get(partitionNo).size() > 0)
            try {
                String fileName = "/data/color-" + partitionNo + "-" + currentFileOffset.get(partitionNo) + ".log";
                Configuration configuration = new Configuration();
                configuration.set("fs.defaultFS", "hdfs://localhost:9000");
                FileSystem hdfsFileSystem = FileSystem.get(configuration);

                FSDataOutputStream fileOutputStream = hdfsFileSystem.create(new Path(fileName));
                fileOutputStream.writeBytes(StringUtils.join(bufferString.get(partitionNo), "\n"));
                fileOutputStream.close();

                // 버퍼 데이터 초기화
                bufferString.put(partitionNo, new ArrayList<>());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
    }

    // 버퍼에 남아있는 모든 데이터를 저장하기 위한 메서드로 컨슈머 스레드 종료 시에 호출된다.
    private void saveRemainBufferToHdfsFile() {
        bufferString.forEach((partitionNo, v) -> this.save(partitionNo));
    }

    public void stopAndWakeup() {
        logger.info("stopAndWakeup");
        consumer.wakeup();
        saveRemainBufferToHdfsFile();
    }
}