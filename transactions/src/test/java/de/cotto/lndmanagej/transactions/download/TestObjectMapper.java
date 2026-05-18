package de.cotto.lndmanagej.transactions.download;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class TestObjectMapper extends JsonMapper {
    public TestObjectMapper() {
        super(JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }
}
