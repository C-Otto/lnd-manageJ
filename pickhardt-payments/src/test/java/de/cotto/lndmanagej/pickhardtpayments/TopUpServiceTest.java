package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.configuration.TopUpConfigurationSettings;
import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus.InstantWithString;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopUpServiceTest {
    private static final Coins AMOUNT = Coins.ofSatoshis(123_000);
    private static final Coins DEFAULT_THRESHOLD = Coins.ofSatoshis(10_000);
    private static final String DESCRIPTION_PREFIX = "Topping up channel with " + PUBKEY + " (alias), adding ";

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

    @BeforeEach
    void setUp() {
        lenient().when(nodeService.getAlias(PUBKEY)).thenReturn("alias");
        lenient().when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
    }

    @Test
    void no_channel_with_peer() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of());
        assertNoTopUp("No channel with 027abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc121 (alias)");
    }

    @Test
    void empty_channel_with_peer() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(Coins.NONE);
        assertTopUp(AMOUNT);
    }

    @Test
    void local_balance_equals_amount() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(AMOUNT);
        assertNoTopUp("Amount 0.000 below threshold 10,000.000 (balance 123,000.000)");
    }

    @Test
    void local_balance_more_than_amount() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(AMOUNT.add(Coins.ofSatoshis(1)));
        assertNoTopUp("Amount -1.000 below threshold 10,000.000 (balance 123,001.000)");
    }

    @Test
    void missing_amount_is_below_threshold() {
        Coins balance = AMOUNT.subtract(DEFAULT_THRESHOLD).add(Coins.ofSatoshis(1));
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(balance);
        assertNoTopUp("Amount 9,999.000 below threshold 10,000.000 (balance 113,001.000)");
    }

    @Test
    void missing_amount_is_threshold() {
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(AMOUNT.subtract(DEFAULT_THRESHOLD));
        assertTopUp(DEFAULT_THRESHOLD);
    }

    @Test
    void missing_amount_is_above_threshold() {
        Coins amount = AMOUNT.subtract(DEFAULT_THRESHOLD).subtract(Coins.ofSatoshis(1));
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(amount);
        assertTopUp(DEFAULT_THRESHOLD.add(Coins.ofSatoshis(1)));
    }

    @Test
    void uses_configured_threshold() {
        Coins threshold = DEFAULT_THRESHOLD.add(Coins.ofSatoshis(1));
        when(configurationService.getIntegerValue(TopUpConfigurationSettings.THRESHOLD))
                .thenReturn(Optional.of((int) threshold.satoshis()));
        Coins balance = AMOUNT.subtract(DEFAULT_THRESHOLD);
        when(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).thenReturn(balance);
        assertNoTopUp("Amount 10,000.000 below threshold 10,001.000 (balance 113,000.000)");
    }

    private void assertTopUp(Coins expectedTopUpAmount) {
        topUpService.topUp(PUBKEY, AMOUNT);
        verify(grpcInvoices).createPaymentRequest(expectedTopUpAmount, DESCRIPTION_PREFIX + expectedTopUpAmount);
    }

    private void assertNoTopUp(String reason) {
        PaymentStatus paymentStatus = topUpService.topUp(PUBKEY, AMOUNT);
        assertThat(paymentStatus.isFailure()).isTrue();
        assertThat(paymentStatus.getMessages().stream().map(InstantWithString::string)).containsExactly(reason);
        verifyNoInteractions(grpcInvoices);
    }
}
