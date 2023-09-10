package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.model.Policy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {
    @InjectMocks
    private PolicyService policyService;

    @Mock
    private GrpcChannelPolicy grpcChannelPolicy;

    @Mock
    private ChannelService channelService;

    @Nested
    class ForLocalChannel {
        @Test
        void getPolicies() {
            PoliciesForLocalChannel expected = new PoliciesForLocalChannel(POLICY_1, POLICY_2);

            when(grpcChannelPolicy.getLocalPolicy(LOCAL_OPEN_CHANNEL.getId())).thenReturn(Optional.of(POLICY_1));
            when(grpcChannelPolicy.getRemotePolicy(LOCAL_OPEN_CHANNEL.getId())).thenReturn(Optional.of(POLICY_2));

            assertThat(policyService.getPolicies(LOCAL_OPEN_CHANNEL)).isEqualTo(expected);
        }

        @Test
        void getPolicies_uses_unknown_policy_if_policy_not_found() {
            // https://github.com/lightningnetwork/lnd/issues/7261
            PoliciesForLocalChannel policies = policyService.getPolicies(LOCAL_OPEN_CHANNEL);
            assertThat(policies.local()).isEqualTo(Policy.UNKNOWN);
            assertThat(policies.remote()).isEqualTo(Policy.UNKNOWN);
        }
    }

    @Nested
    class ForDirectedChannel {
        @Test
        void getPolicyFrom_edge_not_found() {
            when(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY)).thenReturn(Optional.empty());
            assertThat(policyService.getPolicyFrom(CHANNEL_ID, PUBKEY)).isEmpty();
        }

        @Test
        void getPolicyFrom() {
            when(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY)).thenReturn(Optional.of(POLICY_1));
            assertThat(policyService.getPolicyFrom(CHANNEL_ID, PUBKEY)).contains(POLICY_1);
        }

        @Test
        void getPolicyTo_edge_not_found() {
            when(grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY)).thenReturn(Optional.empty());
            assertThat(policyService.getPolicyTo(CHANNEL_ID, PUBKEY)).isEmpty();
        }

        @Test
        void getPolicyTo() {
            when(grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY)).thenReturn(Optional.of(POLICY_1));
            assertThat(policyService.getPolicyTo(CHANNEL_ID, PUBKEY)).contains(POLICY_1);
        }
    }

    @Nested
    class GetMinimumFeeRateFrom {
        @Test
        void no_channel() {
            assertThat(policyService.getMinimumFeeRateFrom(PUBKEY)).isEmpty();
        }

        @Test
        void one_channel() {
            when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
            mockPolicy(CHANNEL_ID, 123);
            assertThat(policyService.getMinimumFeeRateFrom(PUBKEY)).contains(123L);
        }

        @Test
        void two_channels() {
            when(channelService.getOpenChannelsWith(PUBKEY))
                    .thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
            mockPolicy(CHANNEL_ID, 2);
            mockPolicy(CHANNEL_ID_2, 1);
            assertThat(policyService.getMinimumFeeRateFrom(PUBKEY)).contains(1L);
        }

        private void mockPolicy(ChannelId channelId, int feeRate) {
            Policy policy = new Policy(feeRate, Coins.NONE, false, 0, Coins.NONE, Coins.NONE);
            when(grpcChannelPolicy.getPolicyFrom(channelId, PUBKEY))
                    .thenReturn(Optional.of(policy));
        }
    }

    @Nested
    class GetMinimumFeeRateTo {
        @Test
        void no_channel() {
            assertThat(policyService.getMinimumFeeRateTo(PUBKEY)).isEmpty();
        }

        @Test
        void one_channel() {
            when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
            mockPolicy(CHANNEL_ID, 123);
            assertThat(policyService.getMinimumFeeRateTo(PUBKEY)).contains(123L);
        }

        @Test
        void two_channels() {
            when(channelService.getOpenChannelsWith(PUBKEY))
                    .thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
            mockPolicy(CHANNEL_ID, 200);
            mockPolicy(CHANNEL_ID_2, 123);
            assertThat(policyService.getMinimumFeeRateTo(PUBKEY)).contains(123L);
        }

        private void mockPolicy(ChannelId channelId, int feeRate) {
            Policy policy = new Policy(feeRate, Coins.NONE, false, 0, Coins.NONE, Coins.NONE);
            when(grpcChannelPolicy.getPolicyTo(channelId, PUBKEY))
                    .thenReturn(Optional.of(policy));
        }
    }
}
