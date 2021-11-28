package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcFees;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {
    @InjectMocks
    private FeeService feeService;

    @Mock
    private GrpcFees grpcFees;

    @Test
    void getFeeConfiguration() {
        FeeConfiguration expected = new FeeConfiguration(
                789,
                Coins.ofMilliSatoshis(111),
                123,
                Coins.ofMilliSatoshis(456),
                true,
                false
        );

        when(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(789L));
        when(grpcFees.getOutgoingBaseFee(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofMilliSatoshis(111)));
        when(grpcFees.getIncomingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(123L));
        when(grpcFees.getIncomingBaseFee(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofMilliSatoshis(456)));
        when(grpcFees.isEnabledLocal(CHANNEL_ID)).thenReturn(Optional.of(true));
        when(grpcFees.isEnabledRemote(CHANNEL_ID)).thenReturn(Optional.of(false));

        assertThat(feeService.getFeeConfiguration(CHANNEL_ID)).isEqualTo(expected);
    }

    @Test
    void getFeeConfiguration_swapped() {
        FeeConfiguration expected = new FeeConfiguration(
                123,
                Coins.ofMilliSatoshis(456),
                789,
                Coins.ofMilliSatoshis(111),
                false,
                true
        );

        when(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(123L));
        when(grpcFees.getOutgoingBaseFee(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofMilliSatoshis(456)));
        when(grpcFees.getIncomingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(789L));
        when(grpcFees.getIncomingBaseFee(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofMilliSatoshis(111)));
        when(grpcFees.isEnabledLocal(CHANNEL_ID)).thenReturn(Optional.of(false));
        when(grpcFees.isEnabledRemote(CHANNEL_ID)).thenReturn(Optional.of(true));

        assertThat(feeService.getFeeConfiguration(CHANNEL_ID)).isEqualTo(expected);
    }

    @Test
    void getFeeConfiguration_not_found() {
        assertThatIllegalStateException().isThrownBy(() -> feeService.getFeeConfiguration(CHANNEL_ID));
    }
}