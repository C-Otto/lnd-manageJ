package de.cotto.lndmanagej.configuration;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class IniFileReader {
    private final String path;
    private final LoadingCache<String, Map<String, Set<String>>> cache;

    public IniFileReader(@Value("${lndmanagej.configuration-path:}") String path) {
        this.path = path;
        cache = new CacheBuilder()
                .withRefresh(Duration.ofSeconds(5))
                .withExpiry(Duration.ofSeconds(10))
                .build(this::getValuesWithoutCache);
    }

    public Map<String, Set<String>> getValues(String sectionName) {
        return cache.get(sectionName);
    }

    private Map<String, Set<String>> getValuesWithoutCache(String sectionName) {
        return getIni().map(ini -> ini.get(sectionName))
                .map(this::toMultiValueMap)
                .orElse(Map.of());
    }

    private Map<String, Set<String>> toMultiValueMap(Profile.Section section) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (String key : section.keySet()) {
            result.put(key, new HashSet<>(section.getAll(key)));
        }
        return result;
    }

    private Optional<Ini> getIni() {
        try {
            return Optional.of(new Ini(new File(path)));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
