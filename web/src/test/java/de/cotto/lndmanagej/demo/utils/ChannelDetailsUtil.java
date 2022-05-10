package de.cotto.lndmanagej.demo.utils;

import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.FeeReportFixtures;
import de.cotto.lndmanagej.model.FlowReportFixtures;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.RebalanceReportFixtures;
import de.cotto.lndmanagej.ui.dto.ChanDetailsDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.Set;

import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;

public final class ChannelDetailsUtil {

    private ChannelDetailsUtil() {
        // util class
    }

    public static ChanDetailsDto createChannelDetails(OpenChannelDto channel) {
        return new ChanDetailsDto(
                channel.channelId(),
                channel.remotePubkey(),
                channel.remoteAlias(),
                OpenInitiator.REMOTE,
                channel.balanceInformation(),
                OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
                channel.policies(),
                FeeReportDto.createFromModel(FeeReportFixtures.FEE_REPORT),
                FlowReportDto.createFromModel(FlowReportFixtures.FLOW_REPORT),
                RebalanceReportDto.createFromModel(RebalanceReportFixtures.REBALANCE_REPORT),
                Set.of("Something is wrong with this channel."));
    }

}
