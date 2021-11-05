package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.info;

@ExtendWith(MockitoExtension.class)
class ChannelLoggerTest {
    private final TestLogger logger = TestLoggerFactory.getTestLogger(ChannelLogger.class);

    @InjectMocks
    private ChannelLogger channelLogger;

    @Mock
    @SuppressWarnings("unused")
    private GrpcChannels grpcChannels;

    @Test
    void logChannels() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL));
        channelLogger.logChannels();
        assertThat(logger.getLoggingEvents()).contains(info("Channel: {}", CHANNEL));
    }
}