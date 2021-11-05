package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
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
                .withIncomingChannelId(CHANNEL_ID.getShortChannelId())
                .withOutgoingChannelId(CHANNEL_ID_2.getShortChannelId())
                .withTimestamp(789)
                .withIncomingHtlcId(1)
                .withOutgoingHtlcId(2)
                .build();
        assertThat(htlcDetails).isNotNull();
    }

    @Test
    void getTimestamp() {
        assertThat(HTLC_DETAILS.getTimestamp()).isEqualTo(Instant.ofEpochSecond(0, 789));
    }

    @Test
    void withoutTimestamp() {
        assertThat(HTLC_DETAILS.withoutTimestamp().getTimestamp()).isEqualTo(Instant.ofEpochSecond(0));
    }

    @Test
    void testToString() {
        assertThat(HTLC_DETAILS).hasToString(
                "HtlcDetails{" +
                        "incomingChannelId=783231610496155649" +
                        ", outgoingChannelId=879608202739056642" +
                        ", incomingHtlcId=1" +
                        ", outgoingHtlcId=2" +
                        ", timestamp=1970-01-01T00:00:00.000000789Z" +
                        "}"
        );
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(HtlcDetails.class).usingGetClass().verify();
    }
}