package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.info;

@ExtendWith(MockitoExtension.class)
class InfoLoggerTest {
    private final TestLogger logger = TestLoggerFactory.getTestLogger(InfoLogger.class);

    @InjectMocks
    private InfoLogger infoLogger;

    @Mock
    @SuppressWarnings("unused")
    private GrpcGetInfo grpcGetInfo;

    @Test
    void logDetails() {
        when(grpcGetInfo.getAlias()).thenReturn(ALIAS);
        infoLogger.logDetails();
        assertThat(logger.getLoggingEvents()).contains(info("Alias: {}", ALIAS));
    }
}