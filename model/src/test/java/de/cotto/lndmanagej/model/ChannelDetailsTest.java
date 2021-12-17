package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelDetailsFixtures.CHANNEL_DETAILS;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelDetailsTest {
    @Test
    void localChannel() {
        assertThat(CHANNEL_DETAILS.localChannel()).isEqualTo(LOCAL_OPEN_CHANNEL_PRIVATE);
    }

    @Test
    void remoteAlias() {
        assertThat(CHANNEL_DETAILS.remoteAlias()).isEqualTo(ALIAS);
    }

    @Test
    void balanceInformation() {
        assertThat(CHANNEL_DETAILS.balanceInformation()).isEqualTo(BALANCE_INFORMATION);
    }

    @Test
    void onChainCosts() {
        assertThat(CHANNEL_DETAILS.onChainCosts()).isEqualTo(ON_CHAIN_COSTS);
    }

    @Test
    void policies() {
        assertThat(CHANNEL_DETAILS.policies()).isEqualTo(POLICIES);
    }

    @Test
    void feeReport() {
        assertThat(CHANNEL_DETAILS.feeReport()).isEqualTo(FEE_REPORT);
    }

    @Test
    void rebalanceReport() {
        assertThat(CHANNEL_DETAILS.rebalanceReport()).isEqualTo(REBALANCE_REPORT);
    }
}