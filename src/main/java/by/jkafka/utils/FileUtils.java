package by.jkafka.utils;

import by.jkafka.ui.connection.panes.ClusterConfigPane;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileUtils {

    @SneakyThrows
    public static String readResource(String resource) {
        try (var resourceAsStream = ClusterConfigPane.class.getResourceAsStream(resource)) {
            return new String(Objects.requireNonNull(resourceAsStream).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @SneakyThrows
    public static void save(Path path, String source) {
        Files.writeString(path, source);
    }
}
