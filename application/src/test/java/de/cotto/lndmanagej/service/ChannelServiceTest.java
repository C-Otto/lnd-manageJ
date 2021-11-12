package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.ChannelFixtures;
import de.cotto.lndmanagej.model.LocalChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {
    @InjectMocks
    private ChannelService channelService;

    @Mock
    private GrpcChannels grpcChannels;

    @Test
    void getOpenChannelsWith_by_pubkey() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_3));
        assertThat(channelService.getOpenChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(LOCAL_CHANNEL, LOCAL_CHANNEL_3);
    }

    @Test
    void getOpenChannelsWith_ignores_channel_to_other_node() {
        LocalChannel localChannel2 = new LocalChannel(ChannelFixtures.create(PUBKEY, PUBKEY_3, CHANNEL_ID_2), PUBKEY);
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_CHANNEL, localChannel2, LOCAL_CHANNEL_3));
        assertThat(channelService.getOpenChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(LOCAL_CHANNEL, LOCAL_CHANNEL_3);
    }
}