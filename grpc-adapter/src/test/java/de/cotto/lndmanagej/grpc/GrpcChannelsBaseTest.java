package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.OpenInitiator;
import lnrpc.Initiator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcChannelsBaseTest {
    @InjectMocks
    private GrpcChannelsBase grpcChannelsBase;

    @Mock
    private ChannelIdResolver channelIdResolver;

    @Test
    void getOpenInitiator_unknown() {
        assertThat(grpcChannelsBase.getOpenInitiator(Initiator.INITIATOR_UNKNOWN)).isEqualTo(OpenInitiator.UNKNOWN);
    }

    @Test
    void getOpenInitiator_local() {
        assertThat(grpcChannelsBase.getOpenInitiator(Initiator.INITIATOR_LOCAL)).isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void getOpenInitiator_remote() {
        assertThat(grpcChannelsBase.getOpenInitiator(Initiator.INITIATOR_REMOTE)).isEqualTo(OpenInitiator.REMOTE);
    }

    @Test
    void resolveChannelId_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID_2));
        assertThat(grpcChannelsBase.resolveChannelId(CHANNEL_POINT)).contains(CHANNEL_ID_2);
    }

    @Test
    void resolveChannelId_not_resolved() {
        assertThat(grpcChannelsBase.resolveChannelId(CHANNEL_POINT)).isEmpty();
    }
}