package by.jkafka.kafka.read;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface OnReadTopicCallbacks {
    void startReading(String topic);

    void stopReading(String topic);

    default void consume(ConsumerRecord<String, Object> record) {

    }
}
