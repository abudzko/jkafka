package by.jkafka.kafka.info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Partition {
    private int partition;
    private Long commitedOffset;
}
