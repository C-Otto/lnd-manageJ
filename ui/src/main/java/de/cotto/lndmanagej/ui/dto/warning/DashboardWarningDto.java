package de.cotto.lndmanagej.ui.dto.warning;

import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record DashboardWarningDto(
        String alias,
        Pubkey pubkey,
        List<String> nodeWarnings,
        List<ChannelWarningDto> channelWarnings
) {
    public DashboardWarningDto {
        if (alias.isBlank()) {
            alias = pubkey.toString();
        }
    }

    public static DashboardWarningDto forNodeWarnings(Node node, Collection<String> nodeWarnings) {
        return new DashboardWarningDto(node.alias(), node.pubkey(), new ArrayList<>(nodeWarnings), List.of());
    }

    public static DashboardWarningDto forChannelWarnings(
            String alias,
            Pubkey pubkey,
            Collection<ChannelWarningDto> channelWarnings
    ) {
        return new DashboardWarningDto(alias, pubkey, List.of(), new ArrayList<>(channelWarnings));
    }

    public int numberOfWarningItems() {
        return nodeWarnings.size() + channelWarnings.size();
    }
}
