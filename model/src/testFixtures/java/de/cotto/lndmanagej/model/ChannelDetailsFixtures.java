package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT_2;

public class ChannelDetailsFixtures {
    public static final ChannelDetails CHANNEL_DETAILS = new ChannelDetails(
            LOCAL_OPEN_CHANNEL_PRIVATE,
            ALIAS,
            BALANCE_INFORMATION,
            ON_CHAIN_COSTS,
            POLICIES,
            FEE_REPORT,
            REBALANCE_REPORT
    );

    public static final ChannelDetails CHANNEL_DETAILS_2 = new ChannelDetails(
            LOCAL_OPEN_CHANNEL_PRIVATE,
            ALIAS,
            BALANCE_INFORMATION,
            ON_CHAIN_COSTS,
            POLICIES,
            FEE_REPORT,
            REBALANCE_REPORT_2
    );

    public static final ChannelDetails CHANNEL_DETAILS_CLOSED = new ChannelDetails(
            CLOSED_CHANNEL,
            ALIAS,
            BalanceInformation.EMPTY,
            ON_CHAIN_COSTS,
            Policies.UNKNOWN,
            FEE_REPORT,
            REBALANCE_REPORT
    );
}
