package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_DISABLED;
import static org.assertj.core.api.Assertions.assertThat;

class PoliciesForLocalChannelTest {
    @Test
    void local() {
        assertThat(POLICIES_FOR_LOCAL_CHANNEL.local()).isEqualTo(POLICY_DISABLED);
    }

    @Test
    void remote() {
        assertThat(POLICIES_FOR_LOCAL_CHANNEL.remote()).isEqualTo(POLICY_2);
    }

    @Test
    void unknown() {
        assertThat(PoliciesForLocalChannel.UNKNOWN)
                .isEqualTo(new PoliciesForLocalChannel(Policy.UNKNOWN, Policy.UNKNOWN));
    }
}
