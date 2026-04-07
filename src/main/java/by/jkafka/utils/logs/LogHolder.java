package by.jkafka.utils.logs;

import by.jkafka.utils.DateTimeUtils;
import lombok.Builder;
import lombok.Getter;

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
    private final String id;
    private final LinkedBlockingDeque<String> logs;
    private volatile boolean hasNewLogs;

    public synchronized void log(LogEvent event) {
        while (logs.remainingCapacity() <= 0) {
            logs.pollLast();
        }
        var msg = DateTimeUtils.now() + " " + event.getLog();
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
