package de.cotto.lndmanagej.ui.dto.warning;

import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public record DashboardWarningDto(
        String alias,
        Pubkey pubkey,
        Set<String> nodeWarnings,
        Set<ChannelWarningDto> channelWarnings
) {
    public DashboardWarningDto {
        if (alias.isBlank()) {
            alias = pubkey.toString();
        }
    }

    public static DashboardWarningDto forNodeWarnings(Node node, Collection<String> nodeWarnings) {
        return new DashboardWarningDto(node.alias(), node.pubkey(), new LinkedHashSet<>(nodeWarnings), Set.of());
    }

    public static DashboardWarningDto forChannelWarnings(
            String alias,
            Pubkey pubkey,
            Collection<ChannelWarningDto> channelWarnings
    ) {
        return new DashboardWarningDto(alias, pubkey, Set.of(), new LinkedHashSet<>(channelWarnings));
    }

    public int numberOfWarningItems() {
        return nodeWarnings.size() + channelWarnings.size();
    }
}
