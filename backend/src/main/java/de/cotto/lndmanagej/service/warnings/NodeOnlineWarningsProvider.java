package de.cotto.lndmanagej.service.warnings;

import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.warnings.NodeOnlinePercentageWarning;
import de.cotto.lndmanagej.model.warnings.NodeWarning;
import de.cotto.lndmanagej.service.NodeWarningsProvider;
import de.cotto.lndmanagej.service.OnlinePeersService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class NodeOnlineWarningsProvider implements NodeWarningsProvider {
    private static final int ONLINE_PERCENTAGE_THRESHOLD = 80;
    private static final int ONLINE_CHANGES_THRESHOLD = 50;

    private final OnlinePeersService onlinePeersService;

    public NodeOnlineWarningsProvider(
            OnlinePeersService onlinePeersService
    ) {
        this.onlinePeersService = onlinePeersService;
    }

    @Override
    public Stream<NodeWarning> getNodeWarnings(Pubkey pubkey) {
        return Stream.of(
                        (Function<Pubkey, Optional<NodeWarning>>) this::getOnlinePercentageWarning,
                        this::getOnlineChangesWarning
                ).map(function -> function.apply(pubkey))
                .flatMap(Optional::stream);
    }

    private Optional<NodeWarning> getOnlinePercentageWarning(Pubkey pubkey) {
        int percentage = onlinePeersService.getOnlinePercentageLastWeek(pubkey);
        if (percentage < ONLINE_PERCENTAGE_THRESHOLD) {
            return Optional.of(new NodeOnlinePercentageWarning(percentage));
        }
        return Optional.empty();
    }

    private Optional<NodeWarning> getOnlineChangesWarning(Pubkey pubkey) {
        int changes = onlinePeersService.getChangesLastWeek(pubkey);
        if (changes > ONLINE_CHANGES_THRESHOLD) {
            return Optional.of(new NodeOnlineChangesWarning(changes));
        }
        return Optional.empty();
    }
}
