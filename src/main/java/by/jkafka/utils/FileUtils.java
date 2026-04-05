package by.jkafka.utils;

import by.jkafka.ui.connection.panes.config.ClusterConfigPane;
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

    /**
     * Creates parent dirs if not exist
     */
    @SneakyThrows
    public static void save(Path path, String source) {
        var absolutePath = path.toAbsolutePath().normalize();
        Files.createDirectories(absolutePath.getParent());
        Files.writeString(absolutePath, source);
    }
}
