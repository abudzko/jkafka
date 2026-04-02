package by.jkafka.ui.connection.panes.topics.send;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class SendingMessage {
    private String topic;
    private String key;
    // TODO allow several values for one key
    private Map<String, String> headers;
    private List<String> messages;
}
