package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.grpc.GrpcForwardingHistory;
import de.cotto.lndmanagej.model.ForwardingEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ForwardingHistory {
    private final GrpcForwardingHistory grpcForwardingHistory;
    private final ForwardingEventsDao dao;

    public ForwardingHistory(GrpcForwardingHistory grpcForwardingHistory, ForwardingEventsDao dao) {
        this.grpcForwardingHistory = grpcForwardingHistory;
        this.dao = dao;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void refresh() {
        List<ForwardingEvent> events;
        do {
            events = grpcForwardingHistory.getForwardingEventsAfter(dao.getOffset()).orElse(List.of());
            dao.save(events);
        } while (events.size() == grpcForwardingHistory.getLimit());
    }
}
