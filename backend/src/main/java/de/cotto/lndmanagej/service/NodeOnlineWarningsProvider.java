package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.warnings.NodeOnlinePercentageWarning;
import de.cotto.lndmanagej.model.warnings.NodeWarning;
import de.cotto.lndmanagej.service.warnings.NodeWarningsProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_CHANGES_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_PERCENTAGE_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_WARNING_IGNORE_NODE;

@Component
public class NodeOnlineWarningsProvider implements NodeWarningsProvider {
    private static final int DEFAULT_ONLINE_PERCENTAGE_THRESHOLD = 80;
    private static final int DEFAULT_ONLINE_CHANGES_THRESHOLD = 50;

    private final OnlinePeersService onlinePeersService;
    private final ConfigurationService configurationService;

    public NodeOnlineWarningsProvider(
            OnlinePeersService onlinePeersService,
            ConfigurationService configurationService
    ) {
        this.onlinePeersService = onlinePeersService;
        this.configurationService = configurationService;
    }

    @Override
    public Stream<NodeWarning> getNodeWarnings(Pubkey pubkey) {
        if (ignoreWarnings(pubkey)) {
            return Stream.empty();
        }
        return Stream.of(
                        (Function<Pubkey, Optional<NodeWarning>>) this::getOnlinePercentageWarning,
                        this::getOnlineChangesWarning
                ).map(function -> function.apply(pubkey))
                .flatMap(Optional::stream);
    }

    private boolean ignoreWarnings(Pubkey pubkey) {
        return configurationService.getPubkeys(ONLINE_WARNING_IGNORE_NODE).contains(pubkey);
    }

    private Optional<NodeWarning> getOnlinePercentageWarning(Pubkey pubkey) {
        int onlinePercentage = onlinePeersService.getOnlinePercentage(pubkey);
        int daysForOnlinePercentage = onlinePeersService.getDaysForOnlinePercentage();
        if (onlinePercentage < getOnlinePercentageThreshold()) {
            return Optional.of(new NodeOnlinePercentageWarning(onlinePercentage, daysForOnlinePercentage));
        }
        return Optional.empty();
    }

    private Optional<NodeWarning> getOnlineChangesWarning(Pubkey pubkey) {
        int changes = onlinePeersService.getChanges(pubkey);
        int daysForChanges = onlinePeersService.getDaysForChanges();
        if (changes > getOnlineChangesThreshold()) {
            return Optional.of(new NodeOnlineChangesWarning(changes, daysForChanges));
        }
        return Optional.empty();
    }

    private int getOnlinePercentageThreshold() {
        return configurationService.getIntegerValue(ONLINE_PERCENTAGE_THRESHOLD)
                .orElse(DEFAULT_ONLINE_PERCENTAGE_THRESHOLD);
    }

    private int getOnlineChangesThreshold() {
        return configurationService.getIntegerValue(ONLINE_CHANGES_THRESHOLD)
                .orElse(DEFAULT_ONLINE_CHANGES_THRESHOLD);
    }
}
