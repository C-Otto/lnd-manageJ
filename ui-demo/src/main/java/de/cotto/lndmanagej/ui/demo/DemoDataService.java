package de.cotto.lndmanagej.ui.demo;

import de.cotto.lndmanagej.controller.dto.ChannelWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.ui.demo.utils.ChannelDataUtil.createOpenChannel;
import static de.cotto.lndmanagej.ui.demo.utils.ChannelDetailsUtil.createChannelDetails;
import static de.cotto.lndmanagej.ui.demo.utils.NodeDetailsUtil.createNodeDetails;

@Component
public class DemoDataService extends UiDataService {

    public static final OpenChannelDto C_OTTO = createOpenChannel(
            "799999x456x1",
            "c-otto.de",
            "027ce055380348d7812d2ae7745701c9f93e70c1adeb2657f053f91df4f2843c71",
            1_500,
            20_000_000);
    public static final OpenChannelDto ACINQ = createOpenChannel(
            "799999x456x2",
            "ACINQ",
            "03864ef025fde8fb587d989186ce6a4a186895ee44a926bfc370e2c366597a3f8f",
            600_100,
            2_500_000);
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
            8_000_000);
    public static final OpenChannelDto POCKET = createOpenChannel(
            "799999x456x5",
            "PocketBitcoin.com",
            "02765a281bd188e80a89e6ea5092dcb8ebaaa5c5da341e64327e3fadbadcbc686c",
            10_500_900,
            12_000_000);
    public static final OpenChannelDto B_CASH_IS_TRASH = createOpenChannel(
            "799999x456x6",
            "BCash_Is_Trash",
            "0298f6074a454a1f5345cb2a7c6f9fce206cd0bf675d177cdbf0ca7508dd28852f",
            11_100_600,
            9_800_400);
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
            12_400_100,
            1_500_900);
    public static final OpenChannelDto B_CASH_IS_TRASH2 = createOpenChannel(
            "799999x456x9",
            "BCash_Is_Trash",
            "0298f6074a454a1f5345cb2a7c6f9fce206cd0bf675d177cdbf0ca7508dd28852f",
            19_000_100,
            900_900);

    public static final NodeWithWarningsDto ACINQ_WARNING = createNodeWarnings(
            ACINQ.remoteAlias(),
            ACINQ.remotePubkey(),
            "No flow in the past 21 days.",
            "Node has been online 86% in the past 14 days");

    public static final NodeWithWarningsDto POCKET_WARNING = createNodeWarnings(
            POCKET.remoteAlias(),
            POCKET.remotePubkey(),
            "No flow in the past 35 days.");

    public DemoDataService() {
        super();
    }

    @Override
    public StatusModel getStatus() {
        return new StatusModel(true, 735_642, new NodesAndChannelsWithWarningsDto(
                List.of(ACINQ_WARNING, POCKET_WARNING),
                List.of(createChannelWarnings(KRAKEN.channelId(), "Warning channel"))));
    }

    @Override
    public List<OpenChannelDto> getOpenChannels() {
        return List.of(C_OTTO, ACINQ, TRY_BITCOIN, KRAKEN, WOS, B_CASH_IS_TRASH, POCKET, ACINQ2, B_CASH_IS_TRASH2);
    }

    @Override
    public ChannelDetailsDto getChannelDetails(ChannelId channelId) {
        OpenChannelDto localOpenChannel = getOpenChannels().stream()
                .filter(c -> channelId.equals(c.channelId()))
                .findFirst()
                .orElseThrow();
        return createChannelDetails(localOpenChannel);
    }

    @Override
    public NodeDto getNode(Pubkey pubkey) {
        return getOpenChannels().stream()
                .filter(channel -> channel.remotePubkey().equals(pubkey))
                .map(channel -> new NodeDto(pubkey.toString(), channel.remoteAlias(), isOnline(channel)))
                .findFirst().orElseThrow();
    }

    @Override
    public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
        return createNodeDetails(getNode(pubkey));
    }

    private static boolean isOnline(OpenChannelDto channel) {
        return channel.channelId().getShortChannelId() % 4 != 0;
    }


    public static NodeWithWarningsDto createNodeWarnings(String alias, Pubkey pubkey, String... warnings) {
        return new NodeWithWarningsDto(Set.of(warnings), alias, pubkey);
    }

    public static ChannelWithWarningsDto createChannelWarnings(ChannelId channelId, String... warnings) {
        return new ChannelWithWarningsDto(Set.of(warnings), channelId);
    }

}


