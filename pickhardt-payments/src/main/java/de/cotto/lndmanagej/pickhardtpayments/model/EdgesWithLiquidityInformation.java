package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record EdgesWithLiquidityInformation(List<EdgeWithLiquidityInformation> edges, Coins maximumCapacity) {
    public static final EdgesWithLiquidityInformation EMPTY = new EdgesWithLiquidityInformation(List.of());

    public EdgesWithLiquidityInformation(Collection<EdgeWithLiquidityInformation> edges) {
        this(edges.stream().toList(), getMaximumCapacity(edges));
    }

    public EdgesWithLiquidityInformation(EdgeWithLiquidityInformation... edges) {
        this(Arrays.stream(edges).toList());
    }

    private static Coins getMaximumCapacity(Collection<EdgeWithLiquidityInformation> edges) {
        return edges.stream()
                .map(EdgeWithLiquidityInformation::edge)
                .map(Edge::capacity)
                .reduce(Coins.NONE, Coins::maximum);
    }
}
