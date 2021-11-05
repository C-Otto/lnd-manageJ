package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class HtlcDetailsTest {
    @Test
    void builder_without_arguments() {
        assertThatNullPointerException().isThrownBy(() ->
                HtlcDetails.builder().build()
        );
    }

    @Test
    void builder_with_all_arguments() {
        HtlcDetails htlcDetails = HtlcDetails.builder()
                .withIncomingChannelId(CHANNEL_ID.shortChannelId())
                .withOutgoingChannelId(CHANNEL_ID_2.shortChannelId())
                .withTimestamp(789)
                .withIncomingHtlcId(1)
                .withOutgoingHtlcId(2)
                .build();
        assertThat(htlcDetails).isNotNull();
    }

    @Test
    void withoutTimestamp() {
        assertThat(HTLC_DETAILS.withoutTimestamp().timestamp()).isEqualTo(Instant.ofEpochSecond(0));
    }

    @Test
    void timestamp() {
        assertThat(HTLC_DETAILS.timestamp()).isEqualTo(Instant.ofEpochSecond(0, 789));
    }

    @Test
    void incomingHtlcId() {
        assertThat(HTLC_DETAILS.incomingHtlcId()).isEqualTo(1);
    }

    @Test
    void outgoingHtlcId() {
        assertThat(HTLC_DETAILS.outgoingHtlcId()).isEqualTo(2);
    }

    @Test
    void incomingChannelId() {
        assertThat(HTLC_DETAILS.incomingChannelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void outgoingChannelId() {
        assertThat(HTLC_DETAILS.outgoingChannelId()).isEqualTo(CHANNEL_ID_2);
    }
}