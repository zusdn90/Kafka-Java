package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

@SpringBootApplication
public class SpringProducerApplication implements CommandLineRunner {

    private static String TOPIC_NAME = "test";

    @Autowired
    private KafkaTemplate<String, String> customKafkaTemplate;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringProducerApplication.class);
        application.run(args);
    }

    @Override
    public void run(String... args) {
        // ListenableFuture 데이터 전송 이후 정상 적재됐는지 여부를 확인할 경우
        ListenableFuture<SendResult<String, String>> future = customKafkaTemplate.send(TOPIC_NAME, "test");
        future.addCallback(new KafkaSendCallback<String, String>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                // 프로듀서가 보낸 데이터의 브로커 적재 여부가 정상일 경우 호출
            }

            @Override
            public void onFailure(KafkaProducerException ex) {
                // 프로듀서가 보낸 데이터의 브로커 적재 여부가 실패일 경우 호출
            }
        });
        System.exit(0);
    }
}