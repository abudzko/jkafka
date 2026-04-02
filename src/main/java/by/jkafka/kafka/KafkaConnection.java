package by.jkafka.kafka;

import by.jkafka.config.ConnectionConfig;
import by.jkafka.kafka.info.Partition;
import by.jkafka.kafka.info.TopicInfo;
import by.jkafka.kafka.read.OnReadTopicCallbacks;
import by.jkafka.kafka.read.TopicReader;
import by.jkafka.ui.connection.panes.log.Logger;
import by.jkafka.ui.connection.panes.topics.send.SendingMessage;
import lombok.Getter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Getter
public class KafkaConnection {
    private final ConnectionConfig connectionConfig;

    public KafkaConnection(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    private static List<Header> createHeaders(SendingMessage sendingMessage) {
        return sendingMessage.getHeaders().entrySet().stream()
                .map((entry) -> (Header) new RecordHeader(entry.getKey(), entry.getValue().getBytes(StandardCharsets.UTF_8)))
                .collect(Collectors.toList());
    }

    public Boolean testConnection() {
        log("Testing connection: ...");
        int seconds = 3;
        try (var consumer = new KafkaConsumer<>(connectionConfig.properties())) {
            consumer.listTopics(Duration.ofSeconds(seconds));
            log("Testing connection: successfully connected");
            return true;
        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            String msg = "Testing connection: failed to connect due to timeout " + seconds + " sec. " + e.getMessage();
            log(msg);
            Logger.LOGGER.debug(e);
        } catch (Throwable e) {
            String msg = "Testing connection: error in consumer: " + e.getMessage();
            log(msg);
            Logger.LOGGER.debug(e);
        }
        return false;
    }

    public TopicReader topicReader(String topic, OnReadTopicCallbacks readCallbacks) {
        return new TopicReader(topic, this, readCallbacks);
    }

    public void send(SendingMessage sendingMessage) {
        try (var producer = new KafkaProducer<String, String>(connectionConfig.properties())) {
            sendingMessage.getMessages().forEach(m -> {
                var headers = createHeaders(sendingMessage);
                var record = new ProducerRecord<>(sendingMessage.getTopic(), null, sendingMessage.getKey(), m, headers);
                Future<RecordMetadata> future = producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if (e != null) {
                            String msg = "Error sending message: " + e.getMessage();
                            System.err.println(msg);
                            log(msg);
                        } else {
                            String msg = String.format("Message sent successfully to topic %s, partition %d, offset %d%n",
                                    metadata.topic(), metadata.partition(), metadata.offset());
                            log(msg);
                        }
                    }
                });
                try {
                    var result = future.get(3000, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    producer.close();
                    var msg = "Error in producer: " + e.getMessage();
                    Logger.LOGGER.log(msg);
                    Logger.LOGGER.debug(e);
                }
            });
        }
    }

    public List<String> getTopics() {
        log("Getting topics: ...");
        int seconds = 3;
        try (var consumer = new KafkaConsumer<>(connectionConfig.properties())) {
            var topics = consumer.listTopics(Duration.ofSeconds(seconds));
            log("Getting topics: finished. Received " + topics.size() + " topics");
            return new ArrayList<>(topics.keySet());
        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            String msg = "Getting topics: failed to connect due to timeout " + seconds + " sec. " + e.getMessage();
            log(msg);
            Logger.LOGGER.debug(e);
        } catch (Exception e) {
            String msg = "Getting topics: failed to connect: " + e.getMessage();
            log(msg);
            Logger.LOGGER.debug(e);
        }
        return List.of();
    }

    public Optional<TopicInfo> getTopicInfo(String topic) {
        var opName = "Getting topic info";
        log(opName + ": ...");
        int seconds = 3;
        try (var consumer = new KafkaConsumer<>(connectionConfig.properties())) {
            var topicInfo = new TopicInfo();
            topicInfo.setTopic(topic);
            topicInfo.setPartitions(new ArrayList<>());
            var partitionInfos = consumer.partitionsFor(topic, Duration.ofSeconds(seconds));
            var topicPartitions = partitionInfos.stream()
                    .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                    .collect(Collectors.toSet());
            var offsets = consumer.committed(topicPartitions);

            partitionInfos.forEach(partitionInfo -> {
                var partition = new Partition();
                var partitionNumber = partitionInfo.partition();
                partition.setPartition(partitionNumber);
                Optional.ofNullable(offsets.get(new TopicPartition(topic, partitionInfo.partition()))).ifPresent(offsetAndMetadata -> {
                    partition.setCommitedOffset(offsetAndMetadata.offset());
                });
                topicInfo.getPartitions().add(partition);
            });
            log(opName + ": finished");
            return Optional.of(topicInfo);
        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            String msg = opName + ": failed to connect due to timeout " + seconds + " sec. " + e.getMessage();
            log(msg);
            Logger.LOGGER.debug(e);
        } catch (Exception e) {
            String msg = opName + ": failed to connect: " + e.getMessage();
            log(msg);
            Logger.LOGGER.debug(e);
        }
        return Optional.empty();
    }

    private void log(String log) {
        Logger.LOGGER.log(log);
    }
}
