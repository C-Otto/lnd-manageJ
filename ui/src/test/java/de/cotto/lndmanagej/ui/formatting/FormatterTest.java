package de.cotto.lndmanagej.ui.formatting;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatterTest {
    private final Formatter formatter = new Formatter();

    @Test
    void formatNumber_small_number() {
        assertThat(formatter.formatNumber(123)).isEqualTo("123");
    }

    @Test
    void formatNumber_large_number() {
        assertThat(formatter.formatNumber(123_456_781_111L)).isEqualTo("123,456,781,111");
    }

    @Test
    void formatNumber_negative_number() {
        assertThat(formatter.formatNumber(-123_456_781_111L)).isEqualTo("-123,456,781,111");
    }

    @Test
    void formatNumber_zero() {
        assertThat(formatter.formatNumber(0)).isEqualTo("0");
    }
}
