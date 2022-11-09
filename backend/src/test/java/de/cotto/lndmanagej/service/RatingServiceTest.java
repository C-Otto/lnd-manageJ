package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelRating;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ClosedChannelFixtures;
import de.cotto.lndmanagej.model.CoopClosedChannel;
import de.cotto.lndmanagej.model.CoopClosedChannelBuilder;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.MIN_AGE_DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelRatingFixtures.ratingWithValue;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    private static final Duration DEFAULT_MIN_AGE = Duration.ofDays(30);

    private RatingService ratingService;

    @Mock
    private ChannelService channelService;

    @Mock
    private OwnNodeService ownNodeService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private RatingForChannelService ratingForChannelService;

    @BeforeEach
    void setUp() {
        int daysAhead = LOCAL_OPEN_CHANNEL_2.getId().getBlockHeight() + 100 * 24 * 60 / 10;
        lenient().when(ownNodeService.getBlockHeight()).thenReturn(daysAhead);
        lenient().when(configurationService.getIntegerValue(any())).thenReturn(Optional.empty());
        ChannelRating defaultRating = ChannelRating.forChannel(CHANNEL_ID)
                .addValueWithDescription(10_000, "some description");
        lenient().when(ratingForChannelService.getRating(any())).thenReturn(Optional.of(defaultRating));
        OverlappingChannelsService overlappingChannelsService = new OverlappingChannelsService(
                channelService,
                ownNodeService
        );
        ratingService = new RatingService(
                channelService,
                configurationService,
                overlappingChannelsService,
                ratingForChannelService
        );
    }

    @Test
    void getRatingForPeer_no_channel() {
        mockChannels();
        assertThat(ratingService.getRatingForPeer(PUBKEY).isEmpty()).isTrue();
    }

    @Test
    void getRatingForPeer_channel_too_young() {
        when(ownNodeService.getBlockHeight()).thenReturn(LOCAL_OPEN_CHANNEL.getId().getBlockHeight() + 10);
        mockChannels(LOCAL_OPEN_CHANNEL);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2).isEmpty()).isTrue();
    }

    @Test
    void getRatingForPeer_one_channel() {
        mockChannels(LOCAL_OPEN_CHANNEL);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2).orElseThrow().getValue()).isEqualTo(10_000L);
    }

    @Test
    void getRatingForPeer_includes_details() {
        mockChannels(LOCAL_OPEN_CHANNEL);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2).orElseThrow().getDescriptions())
                .containsEntry(PUBKEY_2 + " rating", 10_000L)
                .containsEntry(CHANNEL_ID + " rating", 10_000L);
    }

    @Test
    void getRatingForPeer_two_channels() {
        mockChannels(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2).orElseThrow().getValue()).isEqualTo(2 * 10_000L);
    }

    @Test
    void getRatingForChannel() {
        mockChannels(LOCAL_OPEN_CHANNEL);
        ChannelRating expected = ratingWithValue(123);
        when(ratingForChannelService.getRating(CHANNEL_ID)).thenReturn(Optional.of(expected));
        assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(expected);
    }

    @Test
    void getRatingForChannel_channel_too_young() {
        int defaultMinAge = (int) DEFAULT_MIN_AGE.toDays();
        int blockHeight = CHANNEL_ID.getBlockHeight() + (defaultMinAge - 1) * 24 * 60 / 10;
        when(ownNodeService.getBlockHeight()).thenReturn(blockHeight);
        mockChannels(LOCAL_OPEN_CHANNEL);
        assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getRatingForChannel_channel_too_young_with_configured_min_age() {
        when(configurationService.getIntegerValue(MIN_AGE_DAYS_FOR_ANALYSIS)).thenReturn(Optional.of(40));
        int blockHeight = CHANNEL_ID.getBlockHeight() + 35 * 24 * 60 / 10;
        when(ownNodeService.getBlockHeight()).thenReturn(blockHeight);
        mockChannels(LOCAL_OPEN_CHANNEL);
        assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).isEmpty();
    }

    @Nested
    class YoungOpenChannelWithOverlappingClosedChannel {
        @BeforeEach
        void setUp() {
            int openHeight = CHANNEL_ID.getBlockHeight();
            int defaultMinAge = (int) DEFAULT_MIN_AGE.toDays();
            int blockHeight = openHeight + (defaultMinAge - 1) * 24 * 60 / 10;
            when(ownNodeService.getBlockHeight()).thenReturn(blockHeight);
            CoopClosedChannel closedChannel = getCoopClosedChannel(openHeight - 999, openHeight);
            mockChannels(LOCAL_OPEN_CHANNEL, closedChannel);
        }

        @Test
        void getRatingForChannel() {
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(10_000L);
        }

        @Test
        void getRatingForPeer() {
            assertThat(ratingService.getRatingForPeer(PUBKEY_2).orElseThrow().getValue()).isEqualTo(20_000L);
        }
    }

    @Test
    void young_open_channel_with_overlapping_but_also_young_closed_channel_with_overlapping_old_closed_channel() {
        int openHeightOpenChannel = CHANNEL_ID.getBlockHeight();
        int openHeightClosedChannel = openHeightOpenChannel - 1;
        int openHeightClosedChannelOld = openHeightOpenChannel - 144;

        int defaultMinAge = (int) DEFAULT_MIN_AGE.toDays();
        int blockHeight = openHeightOpenChannel + (defaultMinAge - 1) * 24 * 60 / 10;
        when(ownNodeService.getBlockHeight()).thenReturn(blockHeight);

        CoopClosedChannel closedChannelYoung = getCoopClosedChannel(openHeightClosedChannel, openHeightOpenChannel);
        CoopClosedChannel closedChannelOld = getCoopClosedChannel(openHeightClosedChannelOld, openHeightClosedChannel);
        assumeThat(blockHeight - openHeightClosedChannel).isLessThan(defaultMinAge * 24 * 60 / 10);
        assumeThat(blockHeight - openHeightClosedChannelOld).isGreaterThanOrEqualTo(defaultMinAge * 24 * 60 / 10);

        mockChannels(LOCAL_OPEN_CHANNEL, closedChannelYoung, closedChannelOld);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2).orElseThrow().getValue()).isEqualTo(30_000L);
    }

    @Test
    void young_open_channel_with_non_overlapping_closed_channel() {
        int openHeightOpenChannel = CHANNEL_ID.getBlockHeight();
        int openHeightClosedChannel = openHeightOpenChannel - 1_000;

        int defaultMinAge = (int) DEFAULT_MIN_AGE.toDays();
        int blockHeight = openHeightOpenChannel + (defaultMinAge - 1) * 24 * 60 / 10;
        when(ownNodeService.getBlockHeight()).thenReturn(blockHeight);

        CoopClosedChannel closedChannelWithGap =
                getCoopClosedChannel(openHeightClosedChannel, openHeightOpenChannel - 1);
        assumeThat(blockHeight - openHeightClosedChannel).isGreaterThanOrEqualTo(defaultMinAge * 24 * 60 / 10);

        mockChannels(LOCAL_OPEN_CHANNEL, closedChannelWithGap);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2).isEmpty()).isTrue();
    }

    private void mockChannels(LocalChannel... localChannels) {
        Set<LocalOpenChannel> openChannels = Arrays.stream(localChannels)
                .filter(c -> c instanceof LocalOpenChannel)
                .map(c -> (LocalOpenChannel) c)
                .collect(Collectors.toSet());
        Set<ClosedChannel> closedChannels = Arrays.stream(localChannels)
                .filter(c -> c instanceof ClosedChannel)
                .map(c -> (ClosedChannel) c)
                .collect(Collectors.toSet());
        for (LocalChannel localChannel : localChannels) {
            lenient().when(channelService.getLocalChannel(localChannel.getId()))
                    .thenReturn(Optional.of(localChannel));
        }
        lenient().when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(openChannels);
        for (LocalOpenChannel localOpenChannel : openChannels) {
            lenient().when(channelService.getOpenChannel(localOpenChannel.getId()))
                    .thenReturn(Optional.of(localOpenChannel));
        }
        lenient().when(channelService.getClosedChannelsWith(PUBKEY_2)).thenReturn(closedChannels);
        for (ClosedChannel closedChannel : closedChannels) {
            lenient().when(channelService.getClosedChannel(closedChannel.getId()))
                    .thenReturn(Optional.of(closedChannel));
        }
    }

    private CoopClosedChannel getCoopClosedChannel(int openHeight, int closeHeight) {
        return ClosedChannelFixtures.getWithDefaults(new CoopClosedChannelBuilder())
                .withChannelId(ChannelId.fromCompactForm(openHeight + "x0x0"))
                .withCloseHeight(closeHeight)
                .build();
    }
}
