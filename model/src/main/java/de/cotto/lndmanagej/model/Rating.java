package de.cotto.lndmanagej.model;

import java.util.Map;

public interface Rating {
    long getValue();

    Map<String, Number> getDescriptions();
}
