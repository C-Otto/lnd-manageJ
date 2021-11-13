package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ForwardAttempt;
import de.cotto.lndmanagej.model.ForwardFailure;
import de.cotto.lndmanagej.model.HtlcDetails;
import de.cotto.lndmanagej.model.SettledForward;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.HtlcEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Component
public class GrpcHtlcEvents {
    private final GrpcRouterService grpcRouterService;
    private final Map<HtlcDetails, ForwardAttempt> previousAttempts;

    public GrpcHtlcEvents(GrpcRouterService grpcRouterService) {
        this.grpcRouterService = grpcRouterService;
        previousAttempts = new ConcurrentHashMap<>();
    }

    public Stream<ForwardFailure> getForwardFailures() {
        return getEventStream()
                .filter(this::isForwardFailure)
                .map(this::createForwardFailure)
                .flatMap(Optional::stream);
    }

    public Stream<SettledForward> getSettledForwards() {
        return getEventStream()
                .filter(this::isSettledForward)
                .map(this::createSettledForward)
                .flatMap(Optional::stream);
    }

    private Stream<HtlcEvent> getEventStream() {
        return Stream.iterate(grpcRouterService.getHtlcEvents(), Iterator::hasNext, UnaryOperator.identity())
                .map(Iterator::next)
                .map(this::storeAttempt);
    }

    private HtlcEvent storeAttempt(HtlcEvent htlcEvent) {
        if (isForwardAttempt(htlcEvent)) {
            previousAttempts.put(getHtlcDetails(htlcEvent).withoutTimestamp(), createForwardAttempt(htlcEvent));
        }
        return htlcEvent;
    }

    private boolean isForwardAttempt(HtlcEvent htlcEvent) {
        return htlcEvent.hasForwardEvent()
                && htlcEvent.getIncomingChannelId() > 0
                && htlcEvent.getOutgoingChannelId() > 0;
    }

    private boolean isForwardFailure(HtlcEvent htlcEvent) {
        return htlcEvent.hasForwardFailEvent()
                && htlcEvent.getIncomingChannelId() > 0
                && htlcEvent.getOutgoingChannelId() > 0;
    }

    private boolean isSettledForward(HtlcEvent htlcEvent) {
        return htlcEvent.hasSettleEvent()
                && htlcEvent.getIncomingChannelId() > 0
                && htlcEvent.getOutgoingChannelId() > 0;
    }

    private ForwardAttempt createForwardAttempt(HtlcEvent event) {
        RouterOuterClass.HtlcInfo info = event.getForwardEvent().getInfo();
        return ForwardAttempt.builder()
                .withHtlcDetails(getHtlcDetails(event))
                .withIncomingTimelock(info.getIncomingTimelock())
                .withOutgoingTimelock(info.getOutgoingTimelock())
                .withIncomingAmount(info.getIncomingAmtMsat())
                .withOutgoingAmount(info.getOutgoingAmtMsat())
                .build();
    }

    private Optional<ForwardFailure> createForwardFailure(HtlcEvent event) {
        HtlcDetails htlcDetails = getHtlcDetails(event);
        return getForwardAttemptFor(htlcDetails).map(attempt -> new ForwardFailure(htlcDetails, attempt));
    }

    private Optional<SettledForward> createSettledForward(HtlcEvent event) {
        HtlcDetails htlcDetails = getHtlcDetails(event);
        return getForwardAttemptFor(htlcDetails).map(attempt -> new SettledForward(htlcDetails, attempt));
    }

    private Optional<ForwardAttempt> getForwardAttemptFor(HtlcDetails htlcDetails) {
        ForwardAttempt forwardAttempt = previousAttempts.remove(htlcDetails.withoutTimestamp());
        return Optional.ofNullable(forwardAttempt);
    }

    private HtlcDetails getHtlcDetails(HtlcEvent event) {
        return HtlcDetails.builder()
                .withTimestamp(event.getTimestampNs())
                .withIncomingChannelId(event.getIncomingChannelId())
                .withOutgoingChannelId(event.getOutgoingChannelId())
                .withIncomingHtlcId(event.getIncomingHtlcId())
                .withOutgoingHtlcId(event.getOutgoingHtlcId())
                .build();
    }

    @Scheduled(fixedDelay = 360_000)
    public void removeOldEvents() {
        Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);
        previousAttempts.values().removeIf(event -> event.htlcDetails().timestamp().isBefore(threshold));
    }
}
