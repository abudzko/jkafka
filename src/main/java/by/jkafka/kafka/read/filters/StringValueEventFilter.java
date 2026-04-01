package by.jkafka.kafka.read.filters;

import by.jkafka.utils.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class StringValueEventFilter implements EventFilter {
    private final String filterValue;

    public StringValueEventFilter(String filterValue) {
        this.filterValue = filterValue;
    }

    @Override
    public boolean match(ConsumerRecord<String, Object> record) {
        var value = record.value();
        if (value instanceof String) {
            var strValue = (String) record.value();
            if (StringUtils.hasLength(strValue)) {
                return strValue.contains(filterValue);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "StringValueEventFilter{" +
                "filterValue='" + filterValue + '\'' +
                '}';
    }
}
