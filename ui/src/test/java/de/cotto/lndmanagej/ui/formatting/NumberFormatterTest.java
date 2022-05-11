package de.cotto.lndmanagej.ui.formatting;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberFormatterTest {
    @Test
    void format_small_number() {
        assertThat(NumberFormatter.format(123)).isEqualTo("123");
    }

    @Test
    void format_large_number() {
        assertThat(NumberFormatter.format(123_456_781_111L)).isEqualTo("123,456,781,111");
    }

    @Test
    void format_negative_number() {
        assertThat(NumberFormatter.format(-123_456_781_111L)).isEqualTo("-123,456,781,111");
    }

    @Test
    void format_zero() {
        assertThat(NumberFormatter.format(0)).isEqualTo("0");
    }
}
