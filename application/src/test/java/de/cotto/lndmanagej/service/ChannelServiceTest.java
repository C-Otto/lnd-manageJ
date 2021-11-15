package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelFixtures;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.CLOSED_CHANNEL_UNRESOLVED_ID;
import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.UNRESOLVED_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.UNRESOLVED_CLOSED_CHANNEL_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {
    @InjectMocks
    private ChannelService channelService;

    @Mock
    private GrpcChannels grpcChannels;

    @Mock
    private ChannelIdResolver channelIdResolver;

    @Test
    void getOpenChannelsWith_by_pubkey() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        assertThat(channelService.getOpenChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3);
    }

    @Test
    void getOpenChannelsWith_ignores_channel_to_other_node() {
        Channel channel = ChannelFixtures.create(PUBKEY, PUBKEY_3, CHANNEL_ID_2);
        LocalOpenChannel localOpenChannel2 = new LocalOpenChannel(channel, PUBKEY, BALANCE_INFORMATION);
        when(grpcChannels.getChannels()).thenReturn(
                Set.of(LOCAL_OPEN_CHANNEL, localOpenChannel2, LOCAL_OPEN_CHANNEL_3)
        );
        assertThat(channelService.getOpenChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3);
    }

    @Test
    void getOpenChannels() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        assertThat(channelService.getOpenChannels())
                .containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2);
    }

    @Test
    void getClosedChannels() {
        when(grpcChannels.getUnresolvedClosedChannels())
                .thenReturn(Set.of(UNRESOLVED_CLOSED_CHANNEL, UNRESOLVED_CLOSED_CHANNEL_2));
        assertThat(channelService.getClosedChannels())
                .containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
    }

    @Test
    void getClosedChannels_resolves_id() {
        when(channelIdResolver.resolve(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(grpcChannels.getUnresolvedClosedChannels())
                .thenReturn(Set.of(UNRESOLVED_CLOSED_CHANNEL_2, CLOSED_CHANNEL_UNRESOLVED_ID));
        assertThat(channelService.getClosedChannels())
                .containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
    }

    @Test
    void getClosedChannels_unresolvable_id() {
        when(channelIdResolver.resolve(CHANNEL_POINT)).thenReturn(Optional.empty());
        when(grpcChannels.getUnresolvedClosedChannels())
                .thenReturn(Set.of(UNRESOLVED_CLOSED_CHANNEL_2, CLOSED_CHANNEL_UNRESOLVED_ID));
        assertThat(channelService.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL_2);
    }
}