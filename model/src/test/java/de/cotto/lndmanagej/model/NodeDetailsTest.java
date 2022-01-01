package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.NodeDetailsFixtures.NODE_DETAILS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class NodeDetailsTest {
    @Test
    void pubkey() {
        assertThat(NODE_DETAILS.pubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void alias() {
        assertThat(NODE_DETAILS.alias()).isEqualTo(ALIAS);
    }

    @Test
    void channels() {
        assertThat(NODE_DETAILS.channels()).containsExactly(CHANNEL_ID);
    }

    @Test
    void closedChannels() {
        assertThat(NODE_DETAILS.closedChannels()).containsExactly(CHANNEL_ID_2);
    }

    @Test
    void waitingCloseChannels() {
        assertThat(NODE_DETAILS.waitingCloseChannels()).containsExactly(CHANNEL_ID_3);
    }

    @Test
    void pendingForceClosingChannels() {
        assertThat(NODE_DETAILS.pendingForceClosingChannels()).containsExactly(CHANNEL_ID_4);
    }

    @Test
    void onChainCosts() {
        assertThat(NODE_DETAILS.onChainCosts()).isEqualTo(ON_CHAIN_COSTS);
    }

    @Test
    void balanceInformation() {
        assertThat(NODE_DETAILS.balanceInformation()).isEqualTo(BALANCE_INFORMATION_2);
    }

    @Test
    void onlineReport() {
        assertThat(NODE_DETAILS.onlineReport()).isEqualTo(ONLINE_REPORT);
    }

    @Test
    void feeReport() {
        assertThat(NODE_DETAILS.feeReport()).isEqualTo(FEE_REPORT);
    }

    @Test
    void flowReport() {
        assertThat(NODE_DETAILS.flowReport()).isEqualTo(FLOW_REPORT);
    }

    @Test
    void rebalanceReport() {
        assertThat(NODE_DETAILS.rebalanceReport()).isEqualTo(REBALANCE_REPORT);
    }
}