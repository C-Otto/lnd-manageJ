package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.NodeWarning;
import de.cotto.lndmanagej.model.warnings.NodeWarnings;
import de.cotto.lndmanagej.service.warnings.NodeWarningsProvider;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class NodeWarningsService {
    private final Collection<NodeWarningsProvider> providers;
    private final ChannelService channelService;
    private final NodeService nodeService;

    public NodeWarningsService(
            Collection<NodeWarningsProvider> providers,
            ChannelService channelService,
            NodeService nodeService
    ) {
        this.providers = providers;
        this.channelService = channelService;
        this.nodeService = nodeService;
    }

    public NodeWarnings getNodeWarnings(Pubkey pubkey) {
        Set<NodeWarning> warnings = providers.parallelStream()
                .flatMap(provider -> provider.getNodeWarnings(pubkey))
                .collect(toSet());
        return new NodeWarnings(warnings);
    }

    public Map<Node, NodeWarnings> getNodeWarnings() {
        return channelService.getOpenChannels().parallelStream()
                .map(LocalChannel::getRemotePubkey)
                .distinct()
                .map(pubkey -> new AbstractMap.SimpleEntry<>(pubkey, getNodeWarnings(pubkey)))
                .filter(this::hasWarnings)
                .map(entry -> new AbstractMap.SimpleEntry<>(nodeService.getNode(entry.getKey()), entry.getValue()))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private boolean hasWarnings(Entry<?, NodeWarnings> entry) {
        return !entry.getValue().warnings().isEmpty();
    }
}
