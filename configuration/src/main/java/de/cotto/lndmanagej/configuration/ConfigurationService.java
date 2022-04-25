package de.cotto.lndmanagej.configuration;

import com.google.common.base.Splitter;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Resolution;
import de.cotto.lndmanagej.model.TransactionHash;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
public class ConfigurationService {
    private static final int EXPECTED_NUMBER_OF_COMPONENTS = 3;
    private static final String RESOLUTIONS_SECTION = "resolutions";
    private static final Splitter SPLITTER = Splitter.on(":");
    private static final String ALIASES_SECTION = "aliases";

    private final IniFileReader iniFileReader;

    public ConfigurationService(IniFileReader iniFileReader) {
        this.iniFileReader = iniFileReader;
    }

    public Optional<String> getHardcodedAlias(Pubkey pubkey) {
        Map<String, Set<String>> values = iniFileReader.getValues(ALIASES_SECTION);
        Set<String> alias = values.getOrDefault(pubkey.toString(), Set.of());
        return alias.stream().findFirst();
    }

    public Set<Resolution> getHardcodedResolutions(ChannelId channelId) {
        Map<String, Set<String>> values = iniFileReader.getValues(RESOLUTIONS_SECTION);
        Set<String> forShortChannelId = values.getOrDefault(String.valueOf(channelId.getShortChannelId()), Set.of());
        Set<String> forCompactForm = values.getOrDefault(channelId.getCompactForm(), Set.of());
        Set<String> forCompactFormLnd = values.getOrDefault(channelId.getCompactFormLnd(), Set.of());
        return Stream.of(forShortChannelId, forCompactForm, forCompactFormLnd)
                .flatMap(Set::stream)
                .map(this::parseResolution)
                .flatMap(Optional::stream)
                .collect(toSet());
    }

    private Optional<Resolution> parseResolution(String encodedResolution) {
        try {
            List<String> split = SPLITTER.splitToList(encodedResolution);
            if (split.size() != EXPECTED_NUMBER_OF_COMPONENTS) {
                return Optional.empty();
            }
            String resolutionType = split.get(0);
            String outcome = split.get(1);
            TransactionHash sweepTransaction = TransactionHash.create(split.get(2));
            return Optional.of(new Resolution(Optional.of(sweepTransaction), resolutionType, outcome));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
