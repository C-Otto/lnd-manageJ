package de.cotto.lndmanagej.ui.dto;

import java.util.Comparator;

public record NodeDto(String pubkey, String alias, boolean online, long rating) {

    public static Comparator<NodeDto> getDefaultComparator() {
        return Comparator.comparing(NodeDto::online).thenComparing(NodeDto::alias, String.CASE_INSENSITIVE_ORDER);
    }
}
