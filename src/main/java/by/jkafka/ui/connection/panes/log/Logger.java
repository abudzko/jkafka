package by.jkafka.ui.connection.panes.log;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger {
    public static final Logger LOGGER = new Logger();

    // TODO read from config file
    private final int capacity = 100;
    @Getter
    private final LinkedBlockingDeque<String> logs = new LinkedBlockingDeque<>(capacity);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss");
    @Getter
    private final AtomicBoolean newLogs = new AtomicBoolean(false);

    private Logger() {
    }

    public void clear() {
        logs.clear();
    }

    public synchronized void log(String log) {
        while (logs.size() >= capacity) {
            logs.pollLast();
        }
        String msg = dateTimeFormatter.format(LocalDateTime.now()) + " " + log;
        logs.push(msg);
        newLogs.set(true);
    }

    /**
     * Debug in console
     */
    public void debug(Throwable t) {
        t.printStackTrace();
    }
}
