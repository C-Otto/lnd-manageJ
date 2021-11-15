package de.cotto.lndmanagej.transactions.download;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestObjectMapper extends ObjectMapper {
    public TestObjectMapper() {
        super();
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
