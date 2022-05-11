package de.cotto.lndmanagej.ui.demo.utils;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.OnlineReportDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.List;

import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT_OFFLINE;
import static de.cotto.lndmanagej.ui.demo.utils.DeriveDataUtil.deriveChannelWarnings;
import static de.cotto.lndmanagej.ui.demo.utils.DeriveDataUtil.deriveFeeReport;
import static de.cotto.lndmanagej.ui.demo.utils.DeriveDataUtil.deriveOnChainCosts;
import static de.cotto.lndmanagej.ui.demo.utils.DeriveDataUtil.deriveOpenInitiator;
import static de.cotto.lndmanagej.ui.demo.utils.DeriveDataUtil.derivePolicies;

public final class ChannelDataUtil {

    private ChannelDataUtil() {
        // util class
    }

    public static OpenChannelDto createOpenChannel(
            String compactChannelId,
            String alias,
            String pubkey,
            long local,
            long remote
    ) {
        ChannelId channelId = ChannelId.fromCompactForm(compactChannelId);
        return new OpenChannelDto(
                channelId,
                alias,
                Pubkey.create(pubkey),
                PoliciesDto.createFromModel(derivePolicies(channelId)),
                BalanceInformationDto.createFromModel(new BalanceInformation(
                        Coins.ofSatoshis(local),
                        Coins.ofSatoshis(200),
                        Coins.ofSatoshis(remote),
                        Coins.ofSatoshis(500)
                )));
    }

    public static ChannelDetailsDto createChannelDetails(OpenChannelDto channel) {
        return new ChannelDetailsDto(
                channel.channelId(),
                channel.remotePubkey(),
                channel.remoteAlias(),
                deriveOpenInitiator(channel.channelId()),
                channel.balanceInformation(),
                deriveOnChainCosts(channel.channelId()),
                channel.policies(),
                deriveFeeReport(channel.channelId()),
                DeriveDataUtil.deriveFlowReport(channel.channelId()),
                DeriveDataUtil.deriveRebalanceReport(channel.channelId()),
                DeriveDataUtil.deriveWarnings(channel.channelId()));
    }

    public static NodeDetailsDto createNodeDetails(NodeDto node, List<OpenChannelDto> channels) {
        OpenChannelDto firstChannel = channels.stream().findFirst().orElseThrow();
        OnlineReport onlineReport = node.online() ? ONLINE_REPORT : ONLINE_REPORT_OFFLINE;
        return new NodeDetailsDto(
                Pubkey.create(node.pubkey()),
                node.alias(),
                channels.stream().map(OpenChannelDto::channelId).toList(),
                List.of(ChannelId.fromCompactForm("712345x124x1")),
                List.of(ChannelId.fromCompactForm("712345x124x2")),
                List.of(ChannelId.fromCompactForm("712345x124x3")),
                deriveOnChainCosts(firstChannel.channelId()),
                firstChannel.balanceInformation(),
                OnlineReportDto.createFromModel(onlineReport),
                deriveFeeReport(firstChannel.channelId()),
                DeriveDataUtil.deriveFlowReport(firstChannel.channelId()),
                DeriveDataUtil.deriveRebalanceReport(firstChannel.channelId()),
                deriveChannelWarnings(firstChannel.channelId()));
    }
}
