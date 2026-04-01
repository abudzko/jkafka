package by.jkafka.ui.connection;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ClusterConfigTemplate {
    @Builder.Default
    private ConnectionTemplate template = ConnectionTemplate.DEFAULT;
}
