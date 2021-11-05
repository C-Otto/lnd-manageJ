package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChannelLogger {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GrpcChannels grpcChannels;

    public ChannelLogger(GrpcChannels grpcChannels) {
        this.grpcChannels = grpcChannels;
    }

    @Scheduled(fixedDelay = 60_000)
    public void logChannels() {
        grpcChannels.getChannels()
                .forEach(channel -> logger.info("Channel: {}", channel));
    }
}
