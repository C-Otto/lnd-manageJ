package de.cotto.lndmanagej;

import de.cotto.lndmanagej.controller.LegacyController;
import de.cotto.lndmanagej.grpc.GrpcRouterService;
import de.cotto.lndmanagej.grpc.GrpcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationContextIT {
    @Autowired
    private LegacyController legacyController;

    @MockBean
    @SuppressWarnings("unused")
    private GrpcService grpcService;

    @MockBean
    @SuppressWarnings("unused")
    private GrpcRouterService grpcRouterService;

    @Test
    void contextStarts() {
        assertThat(legacyController).isNotNull();
    }
}