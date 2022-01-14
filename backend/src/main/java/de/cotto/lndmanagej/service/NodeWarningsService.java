package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.NodeWarning;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NodeWarningsService {
    private final Collection<NodeWarningsProvider> providers;

    public NodeWarningsService(Collection<NodeWarningsProvider> providers) {
        this.providers = providers;
    }

    public NodeWarnings getNodeWarnings(Pubkey pubkey) {
        Set<NodeWarning> warnings = providers.stream()
                .flatMap(provider -> provider.getNodeWarnings(pubkey))
                .collect(Collectors.toSet());
        return new NodeWarnings(warnings);
    }
}
