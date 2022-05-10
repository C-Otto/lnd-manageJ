package de.cotto.lndmanagej.demo.utils;

import de.cotto.lndmanagej.controller.dto.ChannelWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.model.PubkeyFixtures;
import de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures;
import de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures;
import de.cotto.lndmanagej.model.warnings.Warning;
import de.cotto.lndmanagej.ui.dto.StatusModel;

import java.util.List;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;

public final class NodeWarningsUtil {

    private NodeWarningsUtil() {
        // util class
    }

    public static StatusModel getStatusModel() {
        return new StatusModel(true, 735_642, createNodeWarnings());
    }

    public static NodesAndChannelsWithWarningsDto createNodeWarnings() {
        return new NodesAndChannelsWithWarningsDto(
                List.of(new NodeWithWarningsDto(NODE_WARNINGS.warnings().stream()
                                .map(Warning::description)
                                .collect(Collectors.toSet()), "WalletOfSatoshi", PUBKEY),
                        new NodeWithWarningsDto(NODE_WARNINGS.warnings().stream()
                                .map(Warning::description)
                                .collect(Collectors.toSet()), "Albert", PUBKEY)
                ),
                List.of(new ChannelWithWarningsDto(CHANNEL_WARNINGS.warnings().stream()
                        .map(Warning::description)
                        .collect(Collectors.toSet()), CHANNEL_ID_3)
                )
        );
    }
}
