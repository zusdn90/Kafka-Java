package com.example;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Processor;

public class FilterProcessor implements Processor<String, String> {

    private ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(String key, String value) {
        if (value.length() > 5) {
            // 다음 토폴로지(다음 프로세서)로 넘어간다.
            context.forward(key, value);
        }
        context.commit();
    }

    @Override
    public void close() {
    }

}