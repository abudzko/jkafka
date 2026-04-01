package by.jkafka.ui.connection;

import lombok.Getter;

@Getter
public enum ConnectionTemplate {
    DEFAULT("/kafka/default-template-properties"),
    SASL_SCRAM("/kafka/sasl-scram-template-properties");

    private final String templatePath;

    ConnectionTemplate(String templatePath) {
        this.templatePath = templatePath;
    }
}
