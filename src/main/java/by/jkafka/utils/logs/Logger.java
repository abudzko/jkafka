package by.jkafka.utils.logs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;

public class Logger {
    public static final Logger LOGGER = new Logger();

    // TODO read from config file
    private final int capacity = 100;
    private final Map<String, LogHolder> logHolderMap = new HashMap<>();

    private Logger() {
    }

    public synchronized void log(LogEvent event) {
        var logHolder = logHolderMap.computeIfAbsent(
                event.getConnectionId(),
                (id) -> LogHolder.builder()
                        .logs(new LinkedBlockingDeque<>(capacity))
                        .build()
        );
        logHolder.log(event);
    }

    public synchronized List<String> getLogs(String connectionId) {
        return Optional.ofNullable(logHolderMap.get(connectionId))
                .map(LogHolder::getLogs)
                .orElseGet(List::of);
    }

    public void clear(String connectionId) {
        Optional.ofNullable(logHolderMap.get(connectionId)).ifPresent(LogHolder::clear);
    }

    /**
     * Debug in console
     */
    public void debug(Throwable t) {
        t.printStackTrace();
    }

    public void console(String log) {
        System.out.println(log);
    }
}
