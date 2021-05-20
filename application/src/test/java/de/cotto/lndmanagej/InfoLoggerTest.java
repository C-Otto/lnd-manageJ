package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static de.cotto.lndmanagej.graph.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.graph.model.NodeFixtures.PUBKEY;
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
    void logAlias() {
        when(grpcGetInfo.getAlias()).thenReturn(ALIAS);
        infoLogger.logAlias();
        assertThat(logger.getLoggingEvents()).contains(info("Alias: {}", ALIAS));
    }

    @Test
    void logPubkey() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
        infoLogger.logPubkey();
        assertThat(logger.getLoggingEvents()).contains(info("Pubkey: {}", PUBKEY));
    }

    @Test
    void logBlockHeight() {
        when(grpcGetInfo.getBlockHeight()).thenReturn(123);
        infoLogger.logBlockHeight();
        assertThat(logger.getLoggingEvents()).contains(info("Block Height: {}", 123));
    }
}