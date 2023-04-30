package com.example;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * 소스 태스크에서 중요한 점은 소스 파일로부터 읽고 토픽으로 보낸 지점을 기록하고 사용한다는 점이다.
 * 소스 파일에서 마지막으로 읽은 지점을 기록하지 않으면 커넥터를 재시작했을 때 데이터가 중복해서 보내질 수 있다.
 * 소스 태스크에서는 마지막 지점을 저장하기 위해 오프셋 스토리지(offset storage)에 데이터를 저장한다.
 */
public class SingleFileSourceTask extends SourceTask {
    private Logger logger = LoggerFactory.getLogger(SingleFileSourceTask.class);

    // 오프셋 스토리지에 저장
    public final String FILENAME_FIELD = "filename";
    public final String POSITION_FIELD = "position";

    private Map<String, String> fileNamePartition;
    private Map<String, Object> offset;
    private String topic;
    private String file;
    private long position = -1;


    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public void start(Map<String, String> props) {
        try {
            // Init variables
            SingleFileSourceConnectorConfig config = new SingleFileSourceConnectorConfig(props);
            topic = config.getString(SingleFileSourceConnectorConfig.TOPIC_NAME);
            file = config.getString(SingleFileSourceConnectorConfig.DIR_FILE_NAME);
            fileNamePartition = Collections.singletonMap(FILENAME_FIELD, file);
            offset = context.offsetStorageReader().offset(fileNamePartition);

            // 오프셋 스토리지에서 가져온 마지막으로 처리한 지점
            // 커넥터가 재시작되더라도 데이터의 중복, 유실 처리를 막을 수 있다.
            if (offset != null) {
                Object lastReadFileOffset = offset.get(POSITION_FIELD);
                if (lastReadFileOffset != null) {
                    position = (Long) lastReadFileOffset;
                }
            } else {
                // 오프셋 스토리지에서 가져온 데이터가 null이라면 파일을 처리한 적이 없다는 뜻이므로 0을 할당
                // 0이면 파일의 첫째 줄부터 처리하여 토픽으로 데이터를 보낸다.
                position = 0;
            }

        } catch (Exception e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    @Override
    public List<SourceRecord> poll() {
        // 소스 파일의 데이터를 읽어서 토픽으로 데이터를 보냄.
        List<SourceRecord> results = new ArrayList<>();
        try {
            Thread.sleep(1000);

            List<String> lines = getLines(position);

            if (lines.size() > 0) {
                lines.forEach(line -> {
                    Map<String, Long> sourceOffset = Collections.singletonMap(POSITION_FIELD, ++position);
                    SourceRecord sourceRecord = new SourceRecord(fileNamePartition, sourceOffset, topic, Schema.STRING_SCHEMA, line);
                    results.add(sourceRecord);
                });
            }
            return results;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ConnectException(e.getMessage(), e);
        }
    }

    private List<String> getLines(long readLine) throws Exception {
        BufferedReader reader = Files.newBufferedReader(Paths.get(file));
        return reader.lines().skip(readLine).collect(Collectors.toList());
    }

    @Override
    public void stop() {
    }
}