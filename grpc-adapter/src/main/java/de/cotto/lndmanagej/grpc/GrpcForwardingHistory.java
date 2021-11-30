package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.ForwardingEvent;
import lnrpc.ForwardingHistoryResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GrpcForwardingHistory {
    private static final int LIMIT = 1_000;

    private final GrpcService grpcService;

    public GrpcForwardingHistory(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public Optional<List<ForwardingEvent>> getForwardingEventsAfter(int offset) {
        ForwardingHistoryResponse response = grpcService.getForwardingHistory(offset, LIMIT).orElse(null);
        if (response == null) {
            return Optional.empty();
        }
        int lastOffsetIndex = response.getLastOffsetIndex();
        int forwardingEventsCount = response.getForwardingEventsCount();
        List<ForwardingEvent> result = new ArrayList<>(forwardingEventsCount);
        int index = lastOffsetIndex - forwardingEventsCount;
        for (lnrpc.ForwardingEvent lndForwardingEvent : response.getForwardingEventsList()) {
            index++;
            result.add(toForwardingEvent(lndForwardingEvent, index));
        }
        return Optional.of(result);
    }

    public int getLimit() {
        return LIMIT;
    }

    private ForwardingEvent toForwardingEvent(lnrpc.ForwardingEvent lndForwardingEvent, int index) {
        Instant timestamp = Instant.ofEpochMilli(lndForwardingEvent.getTimestampNs() / 1_000);
        return new ForwardingEvent(
                index,
                Coins.ofMilliSatoshis(lndForwardingEvent.getAmtInMsat()),
                Coins.ofMilliSatoshis(lndForwardingEvent.getAmtOutMsat()),
                ChannelId.fromShortChannelId(lndForwardingEvent.getChanIdIn()),
                ChannelId.fromShortChannelId(lndForwardingEvent.getChanIdOut()),
                LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC)
        );
    }
}
