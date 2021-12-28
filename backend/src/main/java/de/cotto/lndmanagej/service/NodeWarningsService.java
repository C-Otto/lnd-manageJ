package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.NodeOnlinePercentageWarning;
import de.cotto.lndmanagej.model.NodeWarning;
import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class NodeWarningsService {
    private static final int ONLINE_PERCENTAGE_THRESHOLD = 80;
    private static final int ONLINE_CHANGES_THRESHOLD = 50;

    private final OnlinePeersService onlinePeersService;

    public NodeWarningsService(OnlinePeersService onlinePeersService) {
        this.onlinePeersService = onlinePeersService;
    }

    public NodeWarnings getNodeWarnings(Pubkey pubkey) {
        List<NodeWarning> warnings = Stream.of(
                        (Function<Pubkey, Optional<NodeWarning>>) this::getOnlinePercentageWarning,
                        this::getOnlineChangesWarning
                ).map(function -> function.apply(pubkey))
                .flatMap(Optional::stream)
                .toList();
        return new NodeWarnings(warnings);
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
