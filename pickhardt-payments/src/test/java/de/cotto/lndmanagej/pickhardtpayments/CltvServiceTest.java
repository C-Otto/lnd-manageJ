package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CltvServiceTest {
    private static final Pubkey OWN_PUBKEY = PUBKEY_4;
    private static final Pubkey PEER = PUBKEY_3;

    @InjectMocks
    private CltvService service;

    @Mock
    private PolicyService policyService;

    @Mock
    private ChannelService channelService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @BeforeEach
    void setUp() {
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(OWN_PUBKEY);
    }

    @Test
    void no_last_hop_peer() {
        assertThat(service.getMaximumDeltaForEdges(2016, 100, Optional.empty(), OWN_PUBKEY))
                .isEqualTo(2016 - 100);
    }

    @Test
    void with_last_hop_peer_but_no_channel_known() {
        assertThat(service.getMaximumDeltaForEdges(2016, 100, Optional.of(PEER), OWN_PUBKEY))
                .isEqualTo(2016 - 100);
    }

    @Test
    void one_channel_but_no_known_policy() {
        when(channelService.getOpenChannelsWith(PEER)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(policyService.getPolicyFrom(LOCAL_OPEN_CHANNEL.getId(), PEER)).thenReturn(Optional.empty());
        assertThat(service.getMaximumDeltaForEdges(2016, 100, Optional.of(PEER), OWN_PUBKEY))
                .isEqualTo(2016 - 100);
    }

    @Test
    void one_channel_with_known_policy() {
        when(channelService.getOpenChannelsWith(PEER)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(policyService.getPolicyFrom(LOCAL_OPEN_CHANNEL.getId(), PEER)).thenReturn(Optional.of(POLICY_1));
        assertThat(service.getMaximumDeltaForEdges(2016, 100, Optional.of(PEER), OWN_PUBKEY))
                .isEqualTo(2016 - 100 - POLICY_1.timeLockDelta());
    }

    @Test
    void target_node_is_not_own_node() {
        lenient().when(channelService.getOpenChannelsWith(any())).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        lenient().when(policyService.getPolicyFrom(any(), any())).thenReturn(Optional.of(POLICY_1));
        assertThat(service.getMaximumDeltaForEdges(2016, 100, Optional.of(PEER), PUBKEY_2))
                .isEqualTo(2016 - 100);
    }

    @Test
    void two_channels_with_known_policy() {
        when(channelService.getOpenChannelsWith(PEER)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(policyService.getPolicyFrom(LOCAL_OPEN_CHANNEL.getId(), PEER)).thenReturn(Optional.of(POLICY_1));
        when(policyService.getPolicyFrom(LOCAL_OPEN_CHANNEL_2.getId(), PEER)).thenReturn(Optional.of(POLICY_2));
        assertThat(service.getMaximumDeltaForEdges(2016, 100, Optional.of(PEER), OWN_PUBKEY))
                .isEqualTo(2016 - 100 - POLICY_2.timeLockDelta());
    }
}
