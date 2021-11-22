package de.cotto.lndmanagej.controller;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelIdConverterTest {
    @Test
    void convert() {
        assertThat(new ChannelIdConverter().convert(CHANNEL_ID.toString())).isEqualTo(CHANNEL_ID);
    }

    @Test
    void convert_from_compact_form_with_x() {
        assertThat(new ChannelIdConverter().convert("712345x123x1")).isEqualTo(CHANNEL_ID);
    }

    @Test
    void convert_from_compact_form() {
        assertThat(new ChannelIdConverter().convert("712345:123:1")).isEqualTo(CHANNEL_ID);
    }
}