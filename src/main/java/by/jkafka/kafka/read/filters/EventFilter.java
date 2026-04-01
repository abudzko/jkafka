package by.jkafka.kafka.read.filters;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface EventFilter {
    boolean match(ConsumerRecord<String, Object> record);
}
