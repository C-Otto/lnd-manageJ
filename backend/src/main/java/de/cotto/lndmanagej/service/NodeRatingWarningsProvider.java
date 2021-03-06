package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.configuration.WarningsConfigurationSettings;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.model.warnings.NodeRatingWarning;
import de.cotto.lndmanagej.model.warnings.NodeWarning;
import de.cotto.lndmanagej.service.warnings.NodeWarningsProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class NodeRatingWarningsProvider implements NodeWarningsProvider {
    private static final int DEFAULT_THRESHOLD = 1_000;
    private final ConfigurationService configurationService;
    private final RatingService ratingService;

    public NodeRatingWarningsProvider(
            ConfigurationService configurationService,
            RatingService ratingService
    ) {
        this.configurationService = configurationService;
        this.ratingService = ratingService;
    }

    @Override
    public Stream<NodeWarning> getNodeWarnings(Pubkey pubkey) {
        return Stream.of(getRatingWarning(pubkey)).flatMap(Optional::stream);
    }

    private Optional<NodeWarning> getRatingWarning(Pubkey pubkey) {
        Rating rating = ratingService.getRatingForPeer(pubkey);
        if (rating.isEmpty()) {
            return Optional.empty();
        }
        long threshold = getThreshold();
        long ratingValue = rating.getRating();
        if (ratingValue < threshold) {
            return Optional.of(new NodeRatingWarning(ratingValue, threshold));
        }
        return Optional.empty();
    }

    private long getThreshold() {
        return configurationService.getIntegerValue(WarningsConfigurationSettings.NODE_RATING_THRESHOLD)
                .orElse(DEFAULT_THRESHOLD);
    }
}
