package by.jkafka.kafka.read.filters;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class NoopEventFilter implements EventFilter{
    @Override
    public boolean match(ConsumerRecord<String, Object> record) {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
