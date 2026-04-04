package by.jkafka.utils.logs;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Dedication to connection pane log holder
 * If capacity of log queue is exceeded then old logs are removed
 */
@Getter
@Builder
public class LogHolder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss");
    private final String id;
    private final LinkedBlockingDeque<String> logs;
    private volatile boolean hasNewLogs;

    public synchronized void log(LogEvent event) {
        while (logs.remainingCapacity() <= 0) {
            logs.pollLast();
        }
        var msg = dateTimeFormatter.format(LocalDateTime.now()) + " " + event.getLog();
        logs.push(msg);
        hasNewLogs = true;
    }

    public synchronized List<String> getLogs() {
        if (hasNewLogs) {
            hasNewLogs = false;
            return new ArrayList<>(logs);
        }
        return List.of();
    }

    public void clear() {
        logs.clear();
    }
}
