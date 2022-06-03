package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelCoreInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
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
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_RESERVE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_RESERVE;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    private static final Duration DURATION_FOR_ANALYSIS = Duration.ofDays(30);

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

    @BeforeEach
    void setUp() {
        int daysAhead = LOCAL_OPEN_CHANNEL_2.getId().getBlockHeight() + 100 * 24 * 60 / 10;
        lenient().when(ownNodeService.getBlockHeight()).thenReturn(daysAhead);
        lenient().when(feeService.getFeeReportForChannel(any(), any())).thenReturn(FeeReport.EMPTY);
        lenient().when(rebalanceService.getReportForChannel(any(), any())).thenReturn(RebalanceReport.EMPTY);
    }

    @Test
    void getRatingForPeer_no_channel() {
        assertThat(ratingService.getRatingForPeer(PUBKEY)).isEqualTo(Rating.EMPTY);
    }

    @Test
    void getRatingForPeer_channel_too_young() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        assertThat(ratingService.getRatingForPeer(PUBKEY)).isEqualTo(Rating.EMPTY);
    }

    @Test
    void getRatingForPeer_one_channel() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getOpenChannel(LOCAL_OPEN_CHANNEL.getId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(ratingService.getRatingForPeer(PUBKEY)).isEqualTo(new Rating(1));
    }

    @Test
    void getRatingForPeer_two_channels() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(channelService.getOpenChannel(LOCAL_OPEN_CHANNEL.getId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getOpenChannel(LOCAL_OPEN_CHANNEL_2.getId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_2));
        assertThat(ratingService.getRatingForPeer(PUBKEY)).isEqualTo(new Rating(2));
    }

    @Test
    void getRatingForChannel_not_found() {
        assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getRatingForChannel_channel_too_young() {
        when(ownNodeService.getBlockHeight()).thenReturn(CHANNEL_ID.getBlockHeight() + 29 * 24 * 60 / 10);
        assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(Rating.EMPTY);
    }

    @Nested
    class GetRatingForChannel {
        @BeforeEach
        void setUp() {
            Coins localAvailable = Coins.ofSatoshis(1_000_000);
            LocalOpenChannel localOpenChannel = getLocalOpenChannel(localAvailable);
            when(channelService.getOpenChannel(CHANNEL_ID)).thenReturn(Optional.of(localOpenChannel));
            mockOutgoingFeeRate(0);
        }

        @Test
        void idle() {
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(1));
        }

        @Test
        void earned() {
            Coins feesEarned = Coins.ofMilliSatoshis(123);
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(feesEarned, Coins.NONE));
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(1 + 123));
        }

        @Test
        void sourced() {
            Coins feesSourced = Coins.ofMilliSatoshis(123);
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(Coins.NONE, feesSourced));
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(1 + 123));
        }

        @Test
        void rebalance_source_support() {
            RebalanceReport rebalanceReport = new RebalanceReport(
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.ofMilliSatoshis(1_234),
                    Coins.NONE
            );
            lenient().when(rebalanceService.getReportForChannel(CHANNEL_ID, DURATION_FOR_ANALYSIS))
                    .thenReturn(rebalanceReport);
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(1 + 123));
        }

        @Test
        void rebalance_target_support() {
            RebalanceReport rebalanceReport = new RebalanceReport(
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.NONE,
                    Coins.ofMilliSatoshis(1_234)
            );
            lenient().when(rebalanceService.getReportForChannel(CHANNEL_ID, DURATION_FOR_ANALYSIS))
                    .thenReturn(rebalanceReport);
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating(1 + 123));
        }

        @Test
        void potential_forward_fees() {
            LocalOpenChannel localOpenChannel = getLocalOpenChannel(Coins.ofSatoshis(2_000_000));
            long balanceMilliSat = localOpenChannel.getBalanceInformation().localAvailable().milliSatoshis();
            when(channelService.getOpenChannel(CHANNEL_ID)).thenReturn(Optional.of(localOpenChannel));

            int feeRate = 123_456;
            mockOutgoingFeeRate(feeRate);
            long maxEarnings = (long) (1.0 * feeRate * balanceMilliSat / 1_000 / 1_000_000.0);
            assumeThat(maxEarnings).isGreaterThanOrEqualTo(10);
            assertThat(ratingService.getRatingForChannel(CHANNEL_ID)).contains(new Rating((1 + maxEarnings / 10) / 2));
        }

        @Test
        void scaled_divided_by_million_sats_local() {
            Coins localAvailable = Coins.ofSatoshis(2_500_000);
            long expected = (long) ((1 + 100_000) / 2.5);
            assertScaledRating(localAvailable, expected);
        }

        @Test
        void scaled_not_divided_if_zero_balance() {
            Coins localAvailable = Coins.NONE;
            long expected = 1 + 100_000;
            assertScaledRating(localAvailable, expected);
        }

        @Test
        void scaled_not_divided_if_local_balance_below_one_million() {
            Coins localAvailable = Coins.ofSatoshis(999_999);
            long expected = 1 + 100_000;
            assertScaledRating(localAvailable, expected);
        }

        private void assertScaledRating(Coins localAvailable, long expectedRating) {
            LocalOpenChannel localOpenChannel = getLocalOpenChannel(localAvailable);
            when(channelService.getOpenChannel(CHANNEL_ID)).thenReturn(Optional.of(localOpenChannel));
            when(feeService.getFeeReportForChannel(CHANNEL_ID, DURATION_FOR_ANALYSIS))
                    .thenReturn(new FeeReport(Coins.ofMilliSatoshis(100_000), Coins.NONE));
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
}
