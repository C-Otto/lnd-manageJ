package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ChannelIdTest {

    private static final String ILLEGAL_CHANNEL_ID = "Illegal channel ID";
    private static final String UNEXPECTED_FORMAT = "Unexpected format for compact channel ID";

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class FromCompactForm {
        @Test
        void emptyString() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> ChannelId.fromCompactForm("")
            ).withMessage(UNEXPECTED_FORMAT);
        }

        @Test
        void before_2016() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> ChannelId.fromCompactForm("391176:999:999")
            ).withMessage(ILLEGAL_CHANNEL_ID);
        }

        @Test
        void january_2016() {
            ChannelId channelId = ChannelId.fromCompactForm("391177:0:0");
            assertThat(channelId.getShortChannelId()).isEqualTo(430_103_660_018_532_352L);
        }

        @Test
        void short_channel_id() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> ChannelId.fromCompactForm("774909407114231809")
            ).withMessage(UNEXPECTED_FORMAT);
        }

        @Test
        void with_x() {
            ChannelId channelId = ChannelId.fromCompactForm("704776x2087x1");
            assertThat(channelId.getShortChannelId()).isEqualTo(774_909_407_114_231_809L);
        }

        @Test
        void large_output() {
            ChannelId channelId = ChannelId.fromCompactForm("704776x2087x123");
            assertThat(channelId.getShortChannelId()).isEqualTo(774_909_407_114_231_931L);
        }

        @Test
        void with_colon() {
            ChannelId channelId = ChannelId.fromCompactForm("704776:2087:1");
            assertThat(channelId.getShortChannelId()).isEqualTo(774_909_407_114_231_809L);
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class FromShortChannelId {
        @Test
        void negative() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> ChannelId.fromShortChannelId(-1L)
            ).withMessage(ILLEGAL_CHANNEL_ID);
        }

        @Test
        void zero() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> ChannelId.fromShortChannelId(0L)
            ).withMessage(ILLEGAL_CHANNEL_ID);
        }

        @Test
        void before_2016() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> ChannelId.fromShortChannelId(430_103_660_018_532_351L)
            ).withMessage(ILLEGAL_CHANNEL_ID);
        }

        @Test
        void january_2016() {
            ChannelId channelId = ChannelId.fromShortChannelId(774_909_407_114_231_809L);
            assertThat(channelId.getShortChannelId()).isEqualTo(774_909_407_114_231_809L);
        }

        @Test
        void short_channel_id() {
            ChannelId channelId = ChannelId.fromShortChannelId(774_909_407_114_231_809L);
            assertThat(channelId.getShortChannelId()).isEqualTo(774_909_407_114_231_809L);
        }

        @Test
        void large_output() {
            ChannelId channelId = ChannelId.fromShortChannelId(774_909_407_114_231_931L);
            assertThat(channelId.getShortChannelId()).isEqualTo(774_909_407_114_231_931L);
        }
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ChannelId.class).verify();
    }

    @Test
    void testToString() {
        String expectedString = String.valueOf(ChannelIdFixtures.CHANNEL_ID.getShortChannelId());
        assertThat(ChannelIdFixtures.CHANNEL_ID).hasToString(expectedString);
    }
}