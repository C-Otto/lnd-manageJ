package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.RouteHint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RouteHintService {
    private static final Coins ONE_MILLI_SATOSHI = Coins.ofMilliSatoshis(1);
    private static final Coins FIFTY_COINS = Coins.ofSatoshis(5_000_000_000L);

    private static final Duration MAX_AGE = Duration.ofHours(1);
    private final Map<ChannelId, EdgeWithInstant> edges;

    public RouteHintService() {
        edges = new LinkedHashMap<>();
    }

    public void addDecodedPaymentRequest(DecodedPaymentRequest decodedPaymentRequest) {
        decodedPaymentRequest.routeHints().stream()
                .map(this::toEdge)
                .forEach(edge -> edges.put(edge.channelId(), new EdgeWithInstant(edge)));
    }

    public Set<Edge> getEdgesFromPaymentHints() {
        return edges.values().stream().map(EdgeWithInstant::edge).collect(Collectors.toSet());
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void clean() {
        Instant cutoff = Instant.now().minus(MAX_AGE);
        edges.values().removeIf(edgeWithInstant -> edgeWithInstant.instant().isBefore(cutoff));
    }

    private Edge toEdge(RouteHint routeHint) {
        return new Edge(
                routeHint.channelId(),
                routeHint.sourceNode(),
                routeHint.endNode(),
                FIFTY_COINS,
                toPolicy(routeHint),
                Policy.UNKNOWN
        );
    }

    private Policy toPolicy(RouteHint routeHint) {
        return new Policy(
                routeHint.feeRate(),
                routeHint.baseFee(),
                true,
                routeHint.cltvExpiryDelta(),
                ONE_MILLI_SATOSHI,
                FIFTY_COINS
        );
    }

    @SuppressWarnings("UnusedVariable")
    private record EdgeWithInstant(Edge edge, Instant instant) {
        private EdgeWithInstant(Edge edge) {
            this(edge, Instant.now());
        }
    }
}
