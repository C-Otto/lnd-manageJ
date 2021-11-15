package de.cotto.lndmanagej.transactions.download;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigurationTest {

    @Test
    void test() {
        assertThat(new FeignConfiguration()).isNotNull();
    }
}