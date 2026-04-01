package by.jkafka.kafka.info;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TopicInfo {
    private String topic;
    private List<Partition> partitions;
}
