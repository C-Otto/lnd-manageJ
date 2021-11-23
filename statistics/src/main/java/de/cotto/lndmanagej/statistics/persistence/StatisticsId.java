package de.cotto.lndmanagej.statistics.persistence;

import java.io.Serializable;

public record StatisticsId(Long channelId, long timestamp) implements Serializable {
}