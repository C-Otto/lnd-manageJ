package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {
    @InjectMocks
    private PolicyService policyService;

    @Mock
    private GrpcChannelPolicy grpcChannelPolicy;

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
        void getPolicies_not_found() {
            assertThatIllegalStateException().isThrownBy(() -> policyService.getPolicies(LOCAL_OPEN_CHANNEL));
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
}
