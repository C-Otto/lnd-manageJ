package de.cotto.lndmanagej.model;

import de.cotto.lndmanagej.model.warnings.NodeWarnings;

import java.util.List;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT_OFFLINE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RatingFixtures.RATING;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;

public class NodeDetailsFixtures {
    public static final NodeDetails NODE_DETAILS = new NodeDetails(
            PUBKEY,
            ALIAS,
            List.of(CHANNEL_ID),
            List.of(CHANNEL_ID_2),
            List.of(CHANNEL_ID_3),
            List.of(CHANNEL_ID_4),
            ON_CHAIN_COSTS,
            BALANCE_INFORMATION_2,
            ONLINE_REPORT,
            FEE_REPORT,
            FLOW_REPORT,
            REBALANCE_REPORT,
            NODE_WARNINGS,
            RATING
    );
    public static final NodeDetails NODE_DETAILS_EMPTY = new NodeDetails(
            PUBKEY,
            ALIAS,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            OnChainCosts.NONE,
            BalanceInformation.EMPTY,
            ONLINE_REPORT_OFFLINE,
            FeeReport.EMPTY,
            FlowReport.EMPTY,
            RebalanceReport.EMPTY,
            NodeWarnings.NONE,
            Rating.EMPTY
    );
}
