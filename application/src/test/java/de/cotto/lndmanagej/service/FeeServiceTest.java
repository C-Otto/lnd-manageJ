package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcFees;
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
    void getIncomingFeeRate() {
        when(grpcFees.getIncomingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(123L));
        assertThat(feeService.getIncomingFeeRate(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getIncomingFeeRate_empty() {
        when(grpcFees.getIncomingFeeRate(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThatIllegalStateException().isThrownBy(() -> feeService.getIncomingFeeRate(CHANNEL_ID));
    }

    @Test
    void getOutgoingFeeRate() {
        when(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(Optional.of(123L));
        assertThat(feeService.getOutgoingFeeRate(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getOutgoingFeeRate_empty() {
        when(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThatIllegalStateException().isThrownBy(() -> feeService.getOutgoingFeeRate(CHANNEL_ID));
    }
}