package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcHtlcEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HtlcLogger {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GrpcHtlcEvents grpcHtlcEvents;

    public HtlcLogger(GrpcHtlcEvents grpcHtlcEvents) {
        this.grpcHtlcEvents = grpcHtlcEvents;
    }

    @Scheduled(fixedDelay = 1_000)
    public void logForwardFailures() {
        grpcHtlcEvents.getForwardFailures()
                .forEach(forwardFailure -> logger.info("Forward Failure: {}", forwardFailure));
    }

    @Scheduled(fixedDelay = 1_000)
    public void logSettledForwards() {
        grpcHtlcEvents.getSettledForwards()
                .forEach(settledForward -> logger.info("Settled Forward: {}", settledForward));
    }
}
