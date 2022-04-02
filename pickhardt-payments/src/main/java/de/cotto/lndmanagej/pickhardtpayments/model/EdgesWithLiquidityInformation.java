package de.cotto.lndmanagej.pickhardtpayments.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record EdgesWithLiquidityInformation(List<EdgeWithLiquidityInformation> edges) {
    public static final EdgesWithLiquidityInformation EMPTY = new EdgesWithLiquidityInformation(List.of());

    public EdgesWithLiquidityInformation(Collection<EdgeWithLiquidityInformation> edges) {
        this(edges.stream().toList());
    }

    public EdgesWithLiquidityInformation(EdgeWithLiquidityInformation... edges) {
        this(Arrays.stream(edges).toList());
    }
}
