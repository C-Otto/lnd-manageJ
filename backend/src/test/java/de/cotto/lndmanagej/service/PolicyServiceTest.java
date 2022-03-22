package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcFees;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.model.Policy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {
    @InjectMocks
    private PolicyService policyService;

    @Mock
    private GrpcFees grpcFees;

    @Test
    void getPolicies() {
        PoliciesForLocalChannel expected = new PoliciesForLocalChannel(
                new Policy(789, Coins.ofMilliSatoshis(111), true),
                new Policy(123, Coins.ofMilliSatoshis(456), false)
        );

        mockFees();
        when(grpcFees.isEnabledLocal(CHANNEL_ID)).thenReturn(Optional.of(true));
        when(grpcFees.isEnabledRemote(CHANNEL_ID)).thenReturn(Optional.of(false));

        assertThat(policyService.getPolicies(LOCAL_OPEN_CHANNEL)).isEqualTo(expected);
    }

    @Test
    void getPolicies_enabled_disabled_swapped() {
        PoliciesForLocalChannel expected = new PoliciesForLocalChannel(
                new Policy(789, Coins.ofMilliSatoshis(111), false),
                new Policy(123, Coins.ofMilliSatoshis(456), true)
        );

        mockFees();
        when(grpcFees.isEnabledLocal(CHANNEL_ID)).thenReturn(Optional.of(false));
        when(grpcFees.isEnabledRemote(CHANNEL_ID)).thenReturn(Optional.of(true));

        assertThat(policyService.getPolicies(LOCAL_OPEN_CHANNEL)).isEqualTo(expected);
    }

    @Test
    void getPolicies_not_found() {
        assertThatIllegalStateException().isThrownBy(() -> policyService.getPolicies(LOCAL_OPEN_CHANNEL));
    }

    private void mockFees() {
        when(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(789L));
        when(grpcFees.getOutgoingBaseFee(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofMilliSatoshis(111)));
        when(grpcFees.getIncomingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(123L));
        when(grpcFees.getIncomingBaseFee(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofMilliSatoshis(456)));
    }
}
