package by.jkafka.utils.logs;

import lombok.Builder;
import lombok.Getter;

/**
 * Each connection has its own log pane
 * To save log for target connection pane use connection id
 */
@Getter
@Builder
public class LogEvent {
    private final String connectionId;
    /**
     * Log message
     */
    private final String log;

    public LogEvent(String connectionId, String log) {
        this.connectionId = connectionId;
        this.log = log;
    }
}
