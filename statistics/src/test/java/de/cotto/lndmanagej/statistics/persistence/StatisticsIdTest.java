package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsIdTest {
    @Test
    void test_default_constructor() {
        // required for JPA
        assertThat(new StatisticsId()).isNotNull();
    }
}