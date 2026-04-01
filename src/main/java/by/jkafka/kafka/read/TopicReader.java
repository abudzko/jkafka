package by.jkafka.kafka.read;

import by.jkafka.kafka.KafkaConnection;
import by.jkafka.kafka.read.filters.EventFilter;
import by.jkafka.kafka.read.filters.NoopEventFilter;
import by.jkafka.ui.connection.panes.log.Logger;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class TopicReader {
    @Getter
    private final String topic;
    private final KafkaConnection kafkaConnection;
    private final OnReadTopicCallbacks readCallbacks;
    @Getter
    private volatile boolean started = false;
    private AtomicInteger readEventsCount = new AtomicInteger();
    private volatile EventFilter eventFilter = new NoopEventFilter();

    public TopicReader(
            String topic,
            KafkaConnection kafkaConnection,
            OnReadTopicCallbacks readCallbacks
    ) {
        this.topic = topic;
        this.kafkaConnection = kafkaConnection;
        this.readCallbacks = readCallbacks;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        var t = new Thread(() -> {
            try {
                read();
            } catch (Throwable e) {
                log("Topic reading: failed. " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void read() {
        var properties = kafkaConnection.getConnectionConfig().properties();
        try (var consumer = new KafkaConsumer<String, Object>(properties)) {
            consumer.subscribe(List.of(topic));
            while (started) {
                var records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, Object> record : records) {
                    if (eventFilter.match(record)) {
                        readCallbacks.consume(record);
                    }
                    readEventsCount.incrementAndGet();
                }
                consumer.commitSync();
            }
            var msg = "Topic reading: finished. Topic: " + topic;
            log(msg);
        } catch (Throwable e) {
            var msg = "Error in consumer. Topic: " + topic + ". " + e.getMessage();
            log(msg);
        } finally {
            stop();
            readCallbacks.stopReading(topic);
        }
    }

    public void stop() {
        started = false;
    }

    private void log(String log) {
        Logger.LOGGER.log(log);
    }

    public int readEventsCount() {
        return readEventsCount.get();
    }

    public void updateFilter(EventFilter eventFilter) {
        Logger.LOGGER.log("Event filter applied: " + eventFilter);
        this.eventFilter = Objects.requireNonNullElseGet(eventFilter, NoopEventFilter::new);
    }
}
