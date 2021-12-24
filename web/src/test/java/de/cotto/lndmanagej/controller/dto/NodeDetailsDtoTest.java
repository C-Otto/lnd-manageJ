package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.NodeDetailsFixtures.NODE_DETAILS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class NodeDetailsDtoTest {
    @Test
    void createFromModel() {
        NodeDetailsDto expected = new NodeDetailsDto(
                PUBKEY,
                ALIAS,
                List.of(CHANNEL_ID),
                List.of(CHANNEL_ID_2),
                List.of(CHANNEL_ID_3),
                List.of(CHANNEL_ID_4),
                OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
                BalanceInformationDto.createFromModel(BALANCE_INFORMATION_2),
                OnlineReportDto.createFromModel(ONLINE_REPORT),
                FeeReportDto.createFromModel(FEE_REPORT),
                RebalanceReportDto.createFromModel(REBALANCE_REPORT)
        );
        assertThat(NodeDetailsDto.createFromModel(NODE_DETAILS)).isEqualTo(expected);
    }
}