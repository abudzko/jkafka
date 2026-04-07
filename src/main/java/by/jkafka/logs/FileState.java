package by.jkafka.logs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

@Builder
@Getter
public class FileState {
    @Setter
    private String fileName;
    private final AtomicLong fileSize;
}
