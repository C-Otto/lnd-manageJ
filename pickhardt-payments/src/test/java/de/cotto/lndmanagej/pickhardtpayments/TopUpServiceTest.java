package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.pickhardtpayments.model.InstantWithString;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.ReactiveStreamReader.readAll;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.EXPIRY;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.THRESHOLD;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopUpServiceTest {
    private static final Coins AMOUNT = Coins.ofSatoshis(123_000);
    private static final Coins DEFAULT_THRESHOLD = Coins.ofSatoshis(10_000);
    private static final String DESCRIPTION = "Topping up channel " + CHANNEL_ID + " with " + PUBKEY + " (alias)";
    private static final long OUR_FEE_RATE = 1234;
    private static final long PEER_FEE_RATE = 1233;
    private static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(30);
    private static final int FEE_RATE_WEIGHT = 5;

    @InjectMocks
    private TopUpService topUpService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private GrpcInvoices grpcInvoices;

    @Mock
    private NodeService nodeService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ChannelService channelService;

    @Mock
    private MultiPathPaymentSender multiPathPaymentSender;

    @Mock
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        lenient().when(nodeService.getAlias(PUBKEY)).thenReturn("alias");
        lenient().when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        lenient().when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(PEER_FEE_RATE));
        lenient().when(policyService.getMinimumFeeRateTo(PUBKEY)).thenReturn(Optional.of(OUR_FEE_RATE));
        lenient().when(grpcInvoices.createPaymentRequest(any(), any(), any()))
                .thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));
        lenient().when(multiPathPaymentSender.payPaymentRequest(eq(DECODED_PAYMENT_REQUEST), any()))
                .thenReturn(PaymentStatus.createFor(new HexString("AA00")));
        lenient().when(balanceService.getAvailableRemoteBalanceForPeer(PUBKEY)).thenReturn(AMOUNT);
    }

    @Test
    void no_channel_with_peer() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of());
        assertFailure("No channel with " + PUBKEY + " (alias)");
    }

    @Test
    void unable_to_create_payment_request() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        when(grpcInvoices.createPaymentRequest(any(), any(), any())).thenReturn(Optional.empty());
        assertFailure("Unable to create payment request (" + PUBKEY + ", alias)");
    }

    @Test
    void empty_channel_with_peer() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY);
    }

    @Test
    void local_balance_equals_amount() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(AMOUNT);
        assertFailure("Amount 0.000 below threshold 10,000.000 (balance 123,000.000)");
    }

    @Test
    void local_balance_more_than_amount() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(AMOUNT.add(Coins.ofSatoshis(1)));
        assertFailure("Amount -1.000 below threshold 10,000.000 (balance 123,001.000)");
    }

    @Test
    void missing_amount_is_below_threshold() {
        Coins balance = AMOUNT.subtract(DEFAULT_THRESHOLD).add(Coins.ofSatoshis(1));
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(balance);
        assertFailure("Amount 9,999.000 below threshold 10,000.000 (balance 113,001.000)");
    }

    @Test
    void missing_amount_is_threshold() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(AMOUNT.subtract(DEFAULT_THRESHOLD));
        assertTopUp(DEFAULT_THRESHOLD, DEFAULT_EXPIRY);
    }

    @Test
    void missing_amount_is_above_threshold() {
        Coins balance = AMOUNT.subtract(DEFAULT_THRESHOLD).subtract(Coins.ofSatoshis(1));
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(balance);
        assertTopUp(DEFAULT_THRESHOLD.add(Coins.ofSatoshis(1)), DEFAULT_EXPIRY);
    }

    @Test
    void resulting_amount_is_over_remote_available_balance() {
        Coins localBalance = Coins.NONE;
        Coins remoteBalance = AMOUNT.subtract(Coins.ofSatoshis(1));
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(localBalance);
        when(balanceService.getAvailableRemoteBalanceForPeer(PUBKEY)).thenReturn(remoteBalance);
        assertFailure("Amount 123,000.000 above remote balance 122,999.000");
    }

    @Test
    void peer_charges_more_than_us() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        lenient().when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(OUR_FEE_RATE + 1));
        assertFailure("Peer charges too much: 1235 >= 1234 (" + PUBKEY + ", alias)");
    }

    @Test
    void peer_charges_the_same() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        lenient().when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(OUR_FEE_RATE));
        assertFailure("Peer charges too much: 1234 >= 1234 (" + PUBKEY + ", alias)");
    }

    @Test
    void we_charge_more_than_peer() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY);
    }

    // CPD-OFF
    @Test
    void uses_lower_fee_rate_limit_if_configured() {
        long peerFeeRate = 50L;
        long ourFeeRate = 100L;
        long feeRateLimit = ourFeeRate - 1;
        when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(peerFeeRate));
        when(policyService.getMinimumFeeRateTo(PUBKEY)).thenReturn(Optional.of(ourFeeRate));
        PaymentOptions given = new PaymentOptions(
                Optional.empty(),
                Optional.of(feeRateLimit),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
        PaymentOptions expected = new PaymentOptions(
                Optional.of(5),
                Optional.of(feeRateLimit),
                Optional.of(feeRateLimit - peerFeeRate),
                false,
                Optional.of(PUBKEY),
                Optional.empty()
        );
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY, given, expected);
    }

    @Test
    void ignores_configured_lower_fee_rate_limit_if_too_high() {
        long peerFeeRate = 50L;
        long ourFeeRate = 100L;
        long feeRateLimit = ourFeeRate + 1;
        when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(peerFeeRate));
        when(policyService.getMinimumFeeRateTo(PUBKEY)).thenReturn(Optional.of(ourFeeRate));
        PaymentOptions given = new PaymentOptions(
                Optional.empty(),
                Optional.of(feeRateLimit),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
        PaymentOptions expected = new PaymentOptions(
                Optional.of(5),
                Optional.of(ourFeeRate),
                Optional.of(ourFeeRate - peerFeeRate),
                false,
                Optional.of(PUBKEY),
                Optional.empty()
        );
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY, given, expected);
    }

    @Test
    void adapts_fee_rate_limit_except_incoming_hops_if_fee_rate_limit_is_configured() {
        long peerFeeRate = 50L;
        long ourFeeRate = 100L;
        long feeRateLimit = ourFeeRate - 1;
        when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(peerFeeRate));
        when(policyService.getMinimumFeeRateTo(PUBKEY)).thenReturn(Optional.of(ourFeeRate));
        PaymentOptions given = new PaymentOptions(
                Optional.empty(),
                Optional.of(feeRateLimit),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
        PaymentOptions expected = new PaymentOptions(
                Optional.of(5),
                Optional.of(feeRateLimit),
                Optional.of(feeRateLimit - peerFeeRate),
                false,
                Optional.of(PUBKEY),
                Optional.empty()
        );
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY, given, expected);
    }

    @Test
    void does_not_adapt_fee_rate_limit_except_incoming_hops_if_high_fee_rate_limit_is_configured() {
        long peerFeeRate = 50L;
        long ourFeeRate = 100L;
        long feeRateLimit = ourFeeRate + 1;
        when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(peerFeeRate));
        when(policyService.getMinimumFeeRateTo(PUBKEY)).thenReturn(Optional.of(ourFeeRate));
        PaymentOptions given = new PaymentOptions(
                Optional.empty(),
                Optional.of(feeRateLimit),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
        PaymentOptions expected = new PaymentOptions(
                Optional.of(5),
                Optional.of(ourFeeRate),
                Optional.of(ourFeeRate - peerFeeRate),
                false,
                Optional.of(PUBKEY),
                Optional.empty()
        );
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY, given, expected);
    }

    @Test
    void fee_rate_limit_except_incoming_hops_is_at_least_0() {
        long peerFeeRate = 50L;
        long ourFeeRate = 100L;
        long feeRateLimit = 1;
        when(policyService.getMinimumFeeRateFrom(PUBKEY)).thenReturn(Optional.of(peerFeeRate));
        when(policyService.getMinimumFeeRateTo(PUBKEY)).thenReturn(Optional.of(ourFeeRate));
        PaymentOptions given = new PaymentOptions(
                Optional.empty(),
                Optional.of(feeRateLimit),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
        PaymentOptions expected = new PaymentOptions(
                Optional.of(5),
                Optional.of(feeRateLimit),
                Optional.of(0L),
                false,
                Optional.of(PUBKEY),
                Optional.empty()
        );
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY, given, expected);
    }

    @Test
    void uses_fee_rate_weight_if_configured() {
        int feeRateWeight = 0;
        PaymentOptions given = new PaymentOptions(
                Optional.of(feeRateWeight),
                Optional.empty(),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
        PaymentOptions expected = new PaymentOptions(
                Optional.of(feeRateWeight),
                Optional.of(OUR_FEE_RATE),
                Optional.of(OUR_FEE_RATE - PEER_FEE_RATE),
                false,
                Optional.of(PUBKEY),
                Optional.empty()
        );
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, DEFAULT_EXPIRY, given, expected);
    }
    // CPD-ON

    @Test
    void uses_configured_expiry() {
        int expiry = 900;
        when(configurationService.getIntegerValue(THRESHOLD)).thenReturn(Optional.empty());
        when(configurationService.getIntegerValue(EXPIRY)).thenReturn(Optional.of(expiry));
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT, Duration.ofSeconds(expiry));
    }

    @Test
    void uses_configured_threshold() {
        Coins threshold = DEFAULT_THRESHOLD.add(Coins.ofSatoshis(1));
        when(configurationService.getIntegerValue(THRESHOLD))
                .thenReturn(Optional.of((int) threshold.satoshis()));
        Coins balance = AMOUNT.subtract(DEFAULT_THRESHOLD);
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(balance);
        assertFailure("Amount 10,000.000 below threshold 10,001.000 (balance 113,000.000)");
    }

    private void assertTopUp(Coins expectedTopUpAmount, Duration expiry) {
        PaymentOptions paymentOptions = PaymentOptions.forTopUp(FEE_RATE_WEIGHT, OUR_FEE_RATE, PEER_FEE_RATE, PUBKEY);
        PaymentOptions emptyPaymentOptions = new PaymentOptions(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
        assertTopUp(expectedTopUpAmount, expiry, emptyPaymentOptions, paymentOptions);
    }

    private void assertTopUp(
            Coins expectedTopUpAmount,
            Duration expiry,
            PaymentOptions givenPaymentOptions,
            PaymentOptions expectedPaymentOptions
    ) {
        PaymentStatus paymentStatus = topUpService.topUp(PUBKEY, Optional.empty(), AMOUNT, givenPaymentOptions);
        verify(grpcInvoices).createPaymentRequest(expectedTopUpAmount, DESCRIPTION, expiry);
        verify(multiPathPaymentSender).payPaymentRequest(DECODED_PAYMENT_REQUEST, expectedPaymentOptions);
        assertThat(paymentStatus.isPending()).isTrue();
    }

    private void assertFailure(String reason) {
        PaymentStatus paymentStatus = topUpService.topUp(PUBKEY, Optional.empty(), AMOUNT, DEFAULT_PAYMENT_OPTIONS);
        assertThat(paymentStatus.isFailure()).isTrue();
        assertThat(readAll(paymentStatus)).map(InstantWithString::string).containsExactly(reason);
        verifyNoInteractions(multiPathPaymentSender);
    }
}
