package com.pipeline;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProduceController {
    private final Logger logger = LoggerFactory.getLogger(ProduceController.class);

    // 메시지 키,값은 String 타입으로 설정
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ProduceController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/api/select")
    public void selectColor(
            @RequestHeader("user-agent") String userAgentName,
            @RequestParam(value = "color")String colorName,
            @RequestParam(value = "user")String userName) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        Date now = new Date();
        Gson gson = new Gson();
        UserEventVO userEventVO = new UserEventVO(sdfDate.format(now), userAgentName, colorName, userName);

        // UserEventVO 인스턴스를 JSON포맷의 String 타입으로 변환한다.
        String jsonColorLog = gson.toJson(userEventVO);
        kafkaTemplate.send("select-color", jsonColorLog).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.info(result.toString());
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error(ex.getMessage(), ex);
            }
        });
    }
}
