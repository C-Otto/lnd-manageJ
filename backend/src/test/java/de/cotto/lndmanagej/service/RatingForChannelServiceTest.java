package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelCoreInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.LocalOpenChannelFixtures;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_RESERVE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_RESERVE;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingForChannelServiceTest {
    private static final int ANALYSIS_DAYS = 30;
    private static final Duration DEFAULT_DURATION_FOR_ANALYSIS = Duration.ofDays(ANALYSIS_DAYS);

    private RatingForChannelService ratingForChannelService;

    @Mock
    private ChannelService channelService;

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

    @Mock
    private FlowService flowService;

    @BeforeEach
    void setUp() {
        lenient().when(feeService.getFeeReportForChannel(any(), any()))
                .thenReturn(new FeeReport(Coins.ofMilliSatoshis(10_000 * ANALYSIS_DAYS), Coins.NONE));
        lenient().when(rebalanceService.getReportForChannel(any(), any())).thenReturn(RebalanceReport.EMPTY);
        lenient().when(configurationService.getIntegerValue(any())).thenReturn(Optional.empty());
        lenient().when(balanceService.getLocalBalanceAverage(any(), anyInt()))
                .thenReturn(Optional.of(Coins.ofSatoshis(1_000_000)));
        lenient().when(flowService.getFlowReportForChannel(any(), any())).thenReturn(FlowReport.EMPTY);
        ratingForChannelService = new RatingForChannelService(
                channelService,
                balanceService,
                feeService,
                rebalanceService,
                flowService,
                policyService,
                configurationService
        );

        Coins localAvailable = Coins.ofSatoshis(1_000_000);
        mockOpenChannelChannel(getLocalOpenChannel(localAvailable));
        lenient().when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                .thenReturn(FeeReport.EMPTY);
        mockOutgoingFeeRate(0);
    }

    @Test
    void getRatingForChannel_not_found() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(ratingForChannelService.getRating(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getRatingForChannel_closed() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        Coins feesEarned = Coins.ofMilliSatoshis(123 * ANALYSIS_DAYS);
        when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                .thenReturn(new FeeReport(feesEarned, Coins.NONE));
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).map(Rating::getValue)).contains(123L);
    }

    @Test
    void idle() {
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(0L);
    }

    @Test
    void uses_configured_duration() {
        Duration expectedDuration = Duration.ofDays(42);
        when(configurationService.getIntegerValue(DAYS_FOR_ANALYSIS)).thenReturn(Optional.of(42));
        ratingForChannelService.getRating(CHANNEL_ID);
        verify(feeService).getFeeReportForChannel(CHANNEL_ID, expectedDuration);
    }

    @Test
    void earned() {
        Coins feesEarned = Coins.ofMilliSatoshis(123 * ANALYSIS_DAYS);
        when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                .thenReturn(new FeeReport(feesEarned, Coins.NONE));
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(123L);
    }

    @Test
    void sourced() {
        Coins feesSourced = Coins.ofMilliSatoshis(123 * ANALYSIS_DAYS);
        when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                .thenReturn(new FeeReport(Coins.NONE, feesSourced));
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(123L);
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
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(123L);
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
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(123L);
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
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue())
                .isEqualTo(maxEarnings / 10 / ANALYSIS_DAYS);
    }

    @Test
    void received_via_payments() {
        Coins receivedViaPayments = Coins.ofMilliSatoshis(123_456 * ANALYSIS_DAYS);
        FlowReport flowReport = new FlowReport(
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                receivedViaPayments
        );
        when(flowService.getFlowReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS)).thenReturn(flowReport);
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(123_456L);
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
        long averageSat = ratingForChannelService.getRating(CHANNEL_ID).map(Rating::getValue).orElseThrow();
        assertThat(averageSat).isLessThan(Integer.MAX_VALUE);
    }

    @Test
    void no_average_balance_available() {
        when(balanceService.getLocalBalanceAverage(CHANNEL_ID, ANALYSIS_DAYS))
                .thenReturn(Optional.empty());
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(ratingForChannelService.getRating(CHANNEL_ID)).isEmpty();
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
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(feeService.getFeeReportForChannel(CHANNEL_ID, Duration.ofDays(daysForAnalysis)))
                .thenReturn(new FeeReport(Coins.ofMilliSatoshis(100_000), Coins.NONE));
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue())
                .isEqualTo(100_000L / daysForAnalysis);
    }

    @Test
    void includes_descriptions() {
        Coins feesEarned = Coins.ofMilliSatoshis(300_000L);
        when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                .thenReturn(new FeeReport(feesEarned, Coins.NONE));
        Map<String, Number> expectedDetails = Map.of(
                "712345x123x1 earned", 300_000L,
                "712345x123x1 sourced", 0L,
                "712345x123x1 received via payments", 0L,
                "712345x123x1 support as source", 0L,
                "712345x123x1 support as target", 0L,
                "712345x123x1 future earnings", 0L,
                "712345x123x1 scaled by days", 1.0 / 30,
                "712345x123x1 scaled by liquidity", 1.0d,
                "712345x123x1 rating", 10_000L
        );
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).map(Rating::getDescriptions).orElse(Map.of()))
                .containsExactlyInAnyOrderEntriesOf(expectedDetails);
    }

    private void assertScaledRating(Coins localAvailable, long expectedRating) {
        LocalOpenChannel localOpenChannel = getLocalOpenChannel(localAvailable);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(localOpenChannel));
        when(balanceService.getLocalBalanceAverage(CHANNEL_ID, ANALYSIS_DAYS))
                .thenReturn(Optional.of(localAvailable));
        when(feeService.getFeeReportForChannel(CHANNEL_ID, DEFAULT_DURATION_FOR_ANALYSIS))
                .thenReturn(new FeeReport(Coins.ofMilliSatoshis(100_000 * ANALYSIS_DAYS), Coins.NONE));
        assertThat(ratingForChannelService.getRating(CHANNEL_ID).orElseThrow().getValue()).isEqualTo(expectedRating);
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

    private void mockOpenChannelChannel(LocalOpenChannel localOpenChannel) {
        lenient().when(channelService.getLocalChannel(localOpenChannel.getId()))
                .thenReturn(Optional.of(localOpenChannel));
        lenient().when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of(localOpenChannel));
        lenient().when(channelService.getOpenChannel(localOpenChannel.getId()))
                .thenReturn(Optional.of(localOpenChannel));
    }
}
