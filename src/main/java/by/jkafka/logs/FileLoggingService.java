package by.jkafka.logs;

import by.jkafka.utils.DateTimeUtils;
import by.jkafka.utils.FileUtils;
import by.jkafka.utils.StringUtils;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores Kafka record in file to allow further analysis with 'grep' util<br>
 * If file limit exceeds limit then create new file<br>
 * New file is create per each topic in each connection(pane)<br>
 */
@UtilityClass
public class FileLoggingService {
    private static final int LIMIT_PER_FILE_IN_BYTES = 100 * 1024;
    private static final String LOGS_PATH = "./logs";
    private static final Map<String, FileState> FILE_STATE_MAP = new ConcurrentHashMap<>();

    private static String createFileName(String fileId) {
        return fileId + "-" + DateTimeUtils.now();
    }

    private static String fileId(String connectionId, ConsumerRecord<String, Object> record) {
        return record.topic() + "-" + connectionId;
    }

    /**
     * If size of file exceeds limit then new file will be created
     */
    public static void logInFile(String connectionId, ConsumerRecord<String, Object> record) {
        var fileId = fileId(connectionId, record);
        var fileState = FILE_STATE_MAP.computeIfAbsent(fileId, id -> FileState.builder()
                .fileName(createFileName(fileId))
                .fileSize(new AtomicLong(0))
                .build());


        var log = StringUtils.buildLog(record);
        int logLength = log.length();
        var newFileSize = fileState.getFileSize().addAndGet(logLength);
        if (newFileSize > LIMIT_PER_FILE_IN_BYTES) {
            fileState.getFileSize().set(logLength);
            fileState.setFileName(createFileName(fileId));
        }
        FileUtils.appendToFile(Path.of(LOGS_PATH).resolve(fileState.getFileName()), log);

    }
}
