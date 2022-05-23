package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelStatusDto;
import de.cotto.lndmanagej.controller.dto.ChannelWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.OnlineReportDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelStatus;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.model.BalanceInformation.EMPTY;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT_OFFLINE;
import static de.cotto.lndmanagej.model.OpenCloseStatus.CLOSED;
import static de.cotto.lndmanagej.model.OpenInitiator.UNKNOWN;
import static de.cotto.lndmanagej.ui.demo.data.DeriveDataUtil.deriveChannelStatus;
import static de.cotto.lndmanagej.ui.demo.data.DeriveDataUtil.deriveFeeReport;
import static de.cotto.lndmanagej.ui.demo.data.DeriveDataUtil.deriveFlowReport;
import static de.cotto.lndmanagej.ui.demo.data.DeriveDataUtil.deriveOnChainCosts;
import static de.cotto.lndmanagej.ui.demo.data.DeriveDataUtil.deriveOpenInitiator;
import static de.cotto.lndmanagej.ui.demo.data.DeriveDataUtil.derivePolicies;
import static de.cotto.lndmanagej.ui.demo.data.DeriveDataUtil.deriveRebalanceReport;

@SuppressWarnings("PMD.ExcessiveImports")
@Component
public class DemoDataService extends UiDataService {

    public static final OpenChannelDto C_OTTO = createOpenChannel(
            "799999x456x1",
            "c-otto.de",
            "027ce055380348d7812d2ae7745701c9f93e70c1adeb2657f053f91df4f2843c71",
            1_500,
            19_998_500);
    public static final OpenChannelDto ACINQ = createOpenChannel(
            "799999x456x2",
            "ACINQ",
            "03864ef025fde8fb587d989186ce6a4a186895ee44a926bfc370e2c366597a3f8f",
            500_100,
            2_499_900);
    public static final OpenChannelDto TRY_BITCOIN = createOpenChannel(
            "799999x456x3",
            "try-bitcoin.com",
            "03de6bc7ed1badd0827b99c5b1ad2865322815e761572717a536f0a482864c4427",
            5_700_000,
            14_300_000);
    public static final OpenChannelDto KRAKEN = createOpenChannel(
            "799999x456x4",
            "Kraken 🐙⚡",
            "02f1a8c87607f415c8f22c00593002775941dea48869ce23096af27b0cfdcc0b69",
            5_050_000,
            8_450_000);
    public static final OpenChannelDto POCKET = createOpenChannel(
            "799999x456x5",
            "PocketBitcoin.com",
            "02765a281bd188e80a89e6ea5092dcb8ebaaa5c5da341e64327e3fadbadcbc686c",
            10_500_900,
            11_499_100);
    public static final OpenChannelDto B_CASH_IS_TRASH = createOpenChannel(
            "799999x456x6",
            "BCash_Is_Trash",
            "0298f6074a454a1f5345cb2a7c6f9fce206cd0bf675d177cdbf0ca7508dd28852f",
            11_100_600,
            8_899_400);
    public static final OpenChannelDto WOS = createOpenChannel(
            "799999x456x7",
            "WalletOfSatoshi.com",
            "035e4ff418fc8b5554c5d9eea66396c227bd429a3251c8cbc711002ba215bfc226",
            8_500_000,
            3_500_000);
    public static final OpenChannelDto ACINQ2 = createOpenChannel(
            "799999x456x8",
            "ACINQ",
            "03864ef025fde8fb587d989186ce6a4a186895ee44a926bfc370e2c366597a3f8f",
            11_899_100,
            1_600_900);
    public static final OpenChannelDto B_CASH_IS_TRASH2 = createOpenChannel(
            "799999x456x9",
            "BCash_Is_Trash",
            "0298f6074a454a1f5345cb2a7c6f9fce206cd0bf675d177cdbf0ca7508dd28852f",
            18_099_100,
            900_900);

    public static final NodeWithWarningsDto ACINQ_WARNING = createNodeWithWarnings(
            ACINQ.remoteAlias(),
            ACINQ.remotePubkey(),
            "Node has been online 86% in the past 14 days");

    public static final NodeWithWarningsDto POCKET_WARNING = createNodeWithWarnings(
            POCKET.remoteAlias(),
            POCKET.remotePubkey(),
            "No flow in the past 35 days.");

    public static final ChannelId CLOSED_CHANNEL = ChannelId.fromCompactForm("712345x124x1");

    public DemoDataService() {
        super();
    }

    @Override
    public NodesAndChannelsWithWarningsDto getWarnings() {
        return new NodesAndChannelsWithWarningsDto(List.of(ACINQ_WARNING, POCKET_WARNING), List.of(
                createChannelWarning(B_CASH_IS_TRASH.channelId(), "Channel has accumulated 500000 updates."),
                createChannelWarning(TRY_BITCOIN.channelId(), "Channel has accumulated 600000 updates."),
                createChannelWarning(WOS.channelId(), "Channel has accumulated 700000 updates.")));
    }

    @Override
    public List<OpenChannelDto> getOpenChannels() {
        return List.of(C_OTTO, ACINQ, TRY_BITCOIN, KRAKEN, WOS, B_CASH_IS_TRASH, POCKET, ACINQ2, B_CASH_IS_TRASH2);
    }

    private List<OpenChannelDto> getOpenChannels(Pubkey pubkey) {
        return getOpenChannels().stream()
                .filter(channel -> channel.remotePubkey().equals(pubkey))
                .collect(Collectors.toList());
    }

    @Override
    public ChannelDetailsDto getChannelDetails(ChannelId channelId) throws NotFoundException {
        if (CLOSED_CHANNEL.equals(channelId)) {
            return createClosedChannelDetails(getChannelWarnings(channelId));
        }
        OpenChannelDto localOpenChannel = getOpenChannels().stream()
                .filter(c -> channelId.equals(c.channelId()))
                .findFirst()
                .orElseThrow(NotFoundException::new);
        return createChannelDetails(localOpenChannel, getChannelWarnings(channelId));
    }

    private Set<String> getChannelWarnings(ChannelId channelId) {
        return getWarnings().channelsWithWarnings().stream()
                .filter(c -> c.channelId().equals(channelId))
                .map(ChannelWithWarningsDto::warnings)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public NodeDto getNode(Pubkey pubkey) {
        return getOpenChannels().stream()
                .filter(channel -> channel.remotePubkey().equals(pubkey))
                .map(channel -> new NodeDto(pubkey.toString(), channel.remoteAlias(), isOnline(channel.channelId())))
                .findFirst().orElseThrow();
    }

    @Override
    public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
        List<OpenChannelDto> openChannels = getOpenChannels(pubkey);
        return createNodeDetails(getNode(pubkey), openChannels, getNodeWarnings(pubkey));
    }

    private Set<String> getNodeWarnings(Pubkey pubkey) {
        return getWarnings().nodesWithWarnings().stream()
                .filter(p -> p.pubkey().equals(pubkey))
                .map(NodeWithWarningsDto::warnings)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    static boolean isOnline(ChannelId channelId) {
        return channelId.getShortChannelId() % 4 != 0;
    }

    private static NodeWithWarningsDto createNodeWithWarnings(String alias, Pubkey pubkey, String... warnings) {
        return new NodeWithWarningsDto(Set.of(warnings), alias, pubkey);
    }

    private static ChannelWithWarningsDto createChannelWarning(ChannelId channelId, String... warnings) {
        return new ChannelWithWarningsDto(Set.of(warnings), channelId);
    }

    private static OpenChannelDto createOpenChannel(
            String compactChannelId,
            String alias,
            String pubkey,
            long local,
            long remote
    ) {
        ChannelId channelId = ChannelId.fromCompactForm(compactChannelId);
        Pubkey remotePubkey = Pubkey.create(pubkey);
        PoliciesDto policies = PoliciesDto.createFromModel(derivePolicies(channelId));
        long localReserve = 200;
        long remoteReserve = 500;
        long capacity = local + remote;
        BalanceInformationDto balance = BalanceInformationDto.createFromModel(new BalanceInformation(
                Coins.ofSatoshis(local),
                Coins.ofSatoshis(localReserve),
                Coins.ofSatoshis(remote),
                Coins.ofSatoshis(remoteReserve)
        ));
        return new OpenChannelDto(channelId, alias, remotePubkey, policies, balance, capacity);
    }

    private static ChannelDetailsDto createChannelDetails(OpenChannelDto channel, Set<String> warnings) {
        return new ChannelDetailsDto(
                channel.channelId(),
                channel.remotePubkey(),
                channel.remoteAlias(),
                deriveChannelStatus(channel.channelId()),
                deriveOpenInitiator(channel.channelId()),
                channel.balanceInformation(),
                channel.capacitySat(),
                deriveOnChainCosts(channel.channelId()),
                channel.policies(),
                deriveFeeReport(channel.channelId()),
                deriveFlowReport(channel.channelId()),
                deriveRebalanceReport(channel.channelId()),
                warnings
        );
    }

    private ChannelDetailsDto createClosedChannelDetails(Set<String> warnings) {
        return new ChannelDetailsDto(
                CLOSED_CHANNEL,
                POCKET.remotePubkey(),
                POCKET.remoteAlias(),
                ChannelStatusDto.createFromModel(new ChannelStatus(false, false, true, CLOSED)),
                UNKNOWN,
                BalanceInformationDto.createFromModel(EMPTY),
                10_000_000L,
                deriveOnChainCosts(CLOSED_CHANNEL),
                PoliciesDto.createFromModel(derivePolicies(CLOSED_CHANNEL)),
                deriveFeeReport(CLOSED_CHANNEL),
                deriveFlowReport(CLOSED_CHANNEL),
                deriveRebalanceReport(CLOSED_CHANNEL),
                warnings
        );
    }

    private static NodeDetailsDto createNodeDetails(NodeDto node, List<OpenChannelDto> channels, Set<String> warnings) {
        OpenChannelDto firstChannel = channels.stream().findFirst().orElseThrow();
        OnlineReport onlineReport = node.online() ? ONLINE_REPORT : ONLINE_REPORT_OFFLINE;
        return new NodeDetailsDto(
                Pubkey.create(node.pubkey()),
                node.alias(),
                channels.stream().map(OpenChannelDto::channelId).toList(),
                List.of(CLOSED_CHANNEL),
                List.of(ChannelId.fromCompactForm("712345x124x2")),
                List.of(ChannelId.fromCompactForm("712345x124x3")),
                deriveOnChainCosts(firstChannel.channelId()),
                firstChannel.balanceInformation(),
                OnlineReportDto.createFromModel(onlineReport),
                deriveFeeReport(firstChannel.channelId()),
                deriveFlowReport(firstChannel.channelId()),
                deriveRebalanceReport(firstChannel.channelId()),
                warnings);
    }

}


