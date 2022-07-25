package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelCoreInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ClosedChannelFixtures;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.CoopClosedChannel;
import de.cotto.lndmanagej.model.CoopClosedChannelBuilder;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.LocalOpenChannelFixtures;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.MIN_AGE_DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_RESERVE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_RESERVE;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    private static final int ANALYSIS_DAYS = 30;
    private static final Duration DEFAULT_DURATION_FOR_ANALYSIS = Duration.ofDays(ANALYSIS_DAYS);
    private static final Duration DEFAULT_MIN_AGE = Duration.ofDays(30);

    @InjectMocks
    private RatingService ratingService;

    @Mock
    private ChannelService channelService;

    @Mock
    private OwnNodeService ownNodeService;

    @Mock
    private FeeService feeService;

    @Mock
    private RebalanceService rebalanceService;

    @Mock
    private PolicyService policyService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private BalanceService balanceService;

    @BeforeEach
    void setUp() {
        int daysAhead = LOCAL_OPEN_CHANNEL_2.getId().getBlockHeight() + 100 * 24 * 60 / 10;
        lenient().when(ownNodeService.getBlockHeight()).thenReturn(daysAhead);
        lenient().when(feeService.getFeeReportForChannel(any(), any()))
                .thenReturn(new FeeReport(Coins.ofMilliSatoshis(10_000 * ANALYSIS_DAYS), Coins.NONE));
        lenient().when(rebalanceService.getReportForChannel(any(), any())).thenReturn(RebalanceReport.EMPTY);
        lenient().when(configurationService.getIntegerValue(any())).thenReturn(Optional.empty());
        lenient().when(balanceService.getLocalBalanceAverage(any(), anyInt()))
                .thenReturn(Optional.of(Coins.ofSatoshis(1_000_000)));
    }

    @Test
    void getRatingForPeer_no_channel() {
        mockChannels();
        assertThat(ratingService.getRatingForPeer(PUBKEY)).isEqualTo(Rating.EMPTY);
    }

    @Test
    void getRatingForPeer_channel_too_young() {
        when(ownNodeService.getBlockHeight()).thenReturn(LOCAL_OPEN_CHANNEL.getId().getBlockHeight() + 10);
        mockChannels(LOCAL_OPEN_CHANNEL);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2)).isEqualTo(Rating.EMPTY);
    }

    @Test
    void getRatingForPeer_one_channel() {
        mockChannels(LOCAL_OPEN_CHANNEL);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2)).isEqualTo(new Rating(10_000));
    }

    @Test
    void getRatingForPeer_two_channels() {
        mockChannels(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2);
        assertThat(ratingService.getRatingForPeer(PUBKEY_2)).isEqualTo(new Rating(2 * 10_000));
    }

    @Test
    void getRatingForChannel_not_found() {
        mockChannels(LOCAL_OPEN_CHANNEL_2);
        assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getRatingForChannel_not_found_on_second_request() {
        mockChannels(LOCAL_OPEN_CHANNEL_2);
        when(channelService.getLocalChannel(CHANNEL_ID_2)).thenReturn(Optional.empty());
        assertThat(ratingService.getRatingForChannel(CHANNEL_ID_2)).isEmpty();
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
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(10_000));
        }

        @Test
        void getRatingForPeer() {
            assertThat(ratingService.getRatingForPeer(PUBKEY_2)).isEqualTo(new Rating(20_000));
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
        assertThat(ratingService.getRatingForPeer(PUBKEY_2)).isEqualTo(new Rating(30_000));
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
        assertThat(ratingService.getRatingForPeer(PUBKEY_2)).isEqualTo(Rating.EMPTY);
    }

    @Nested
    class GetRatingForChannel {
        @BeforeEach
        void setUp() {
            Coins localAvailable = Coins.ofSatoshis(1_000_000);
            LocalOpenChannel localOpenChannel = getLocalOpenChannel(localAvailable);
            mockChannels(localOpenChannel);
            lenient().when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(FeeReport.EMPTY);
            mockOutgoingFeeRate(0);
        }

        @Test
        void idle() {
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(0));
        }

        @Test
        void uses_configured_duration() {
            Duration expectedDuration = Duration.ofDays(42);
            when(configurationService.getIntegerValue(DAYS_FOR_ANALYSIS)).thenReturn(Optional.of(42));
            ratingService.getRatingForChannel(CHANNEL_ID);
            verify(feeService).getFeeReportForChannel(CHANNEL_ID, expectedDuration);
        }

        @Test
        void earned() {
            Coins feesEarned = Coins.ofMilliSatoshis(123 * ANALYSIS_DAYS);
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(feesEarned, Coins.NONE));
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(123));
        }

        @Test
        void sourced() {
            Coins feesSourced = Coins.ofMilliSatoshis(123 * ANALYSIS_DAYS);
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(Coins.NONE, feesSourced));
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(123));
        }

        @Test
        void rebalance_source_support() {
            RebalanceReport rebalanceReport = new RebalanceReport(
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.ofMilliSatoshis(1_234_567 * ANALYSIS_DAYS),
                    Coins.NONE
            );
            lenient().when(rebalanceService.getReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(rebalanceReport);
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(123));
        }

        @Test
        void rebalance_target_support() {
            RebalanceReport rebalanceReport = new RebalanceReport(
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.ofMilliSatoshis(1_234_567 * ANALYSIS_DAYS)
            );
            lenient().when(rebalanceService.getReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(rebalanceReport);
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(123));
        }

        @Test
        void potential_forward_fees() {
            LocalOpenChannel localOpenChannel = getLocalOpenChannel(Coins.ofSatoshis(2_000_000));
            long balanceMilliSat = localOpenChannel.getBalanceInformation().localAvailable().milliSatoshis();
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(localOpenChannel));

            int feeRate = 123_456;
            mockOutgoingFeeRate(feeRate);
            long maxEarnings = (long) (1.0 * feeRate * balanceMilliSat / 1_000 / 1_000_000.0);
            assumeThat(maxEarnings).isGreaterThanOrEqualTo(10 * ANALYSIS_DAYS);
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID))
                    .contains(new Rating(maxEarnings / 10 / ANALYSIS_DAYS));
        }

        @Test
        void divided_by_average_million_sats_local() {
            Coins localAvailableAverage = Coins.ofSatoshis(2_500_000);
            long expected = (long) ((1 + 100_000) / 2.5);
            assertScaledRating(localAvailableAverage, expected);
        }

        @Test
        void zero_balance_on_average() {
            when(balanceService.getLocalBalanceAverage(CHANNEL_ID, ANALYSIS_DAYS))
                    .thenReturn(Optional.of(Coins.NONE));
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(Coins.ofMilliSatoshis(100_000 * ANALYSIS_DAYS), Coins.NONE));
            assertThatCode(() -> ratingService.getRatingForChannel(CHANNEL_ID)).doesNotThrowAnyException();
        }

        @Test
        void no_average_balance_available() {
            when(balanceService.getLocalBalanceAverage(CHANNEL_ID, ANALYSIS_DAYS))
                    .thenReturn(Optional.empty());
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(Coins.ofMilliSatoshis(200_000 * ANALYSIS_DAYS), Coins.NONE));
            assertThatCode(() -> ratingService.getRatingForChannel(CHANNEL_ID)).doesNotThrowAnyException();
        }

        @Test
        void divided_by_average_million_sats_local_if_less_than_one_million_sat() {
            Coins localAvailable = Coins.ofSatoshis(500_000);
            long expected = 100_000 * 2;
            assertScaledRating(localAvailable, expected);
        }

        @Test
        void scaled_by_days() {
            int daysForAnalysis = 123;
            when(configurationService.getIntegerValue(DAYS_FOR_ANALYSIS)).thenReturn(Optional.of(daysForAnalysis));
            int blockHeight = CHANNEL_ID.getBlockHeight() + 1_000 * 24 * 60 / 10;
            when(ownNodeService.getBlockHeight()).thenReturn(blockHeight);
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
            when(feeService.getFeeReportForChannel(CHANNEL_ID, Duration.ofDays(daysForAnalysis)))
                    .thenReturn(new FeeReport(Coins.ofMilliSatoshis(100_000), Coins.NONE));
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(100_000 / daysForAnalysis));
        }

        private void assertScaledRating(Coins localAvailable, long expectedRating) {
            LocalOpenChannel localOpenChannel = getLocalOpenChannel(localAvailable);
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(localOpenChannel));
            when(balanceService.getLocalBalanceAverage(CHANNEL_ID, ANALYSIS_DAYS))
                    .thenReturn(Optional.of(localAvailable));
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(Coins.ofMilliSatoshis(100_000 * ANALYSIS_DAYS), Coins.NONE));
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(expectedRating));
        }

        private LocalOpenChannel getLocalOpenChannel(Coins localAvailable) {
            BalanceInformation balanceInformation = new BalanceInformation(
                    localAvailable.add(LOCAL_RESERVE),
                    LOCAL_RESERVE,
                    REMOTE_BALANCE,
                    REMOTE_RESERVE
            );
            return new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                    PUBKEY,
                    PUBKEY_2,
                    balanceInformation,
                    LOCAL,
                    LocalOpenChannelFixtures.TOTAL_SENT,
                    LocalOpenChannelFixtures.TOTAL_RECEIVED,
                    false,
                    true,
                    LocalOpenChannelFixtures.NUM_UPDATES
            );
        }

        private void mockOutgoingFeeRate(long feeRate) {
            lenient().when(policyService.getMinimumFeeRateTo(LOCAL_OPEN_CHANNEL.getRemotePubkey()))
                    .thenReturn(Optional.of(feeRate));
        }
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
