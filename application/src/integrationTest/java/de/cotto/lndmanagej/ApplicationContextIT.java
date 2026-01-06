package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcInvoicesService;
import de.cotto.lndmanagej.grpc.GrpcRouterService;
import de.cotto.lndmanagej.grpc.GrpcService;
import de.cotto.lndmanagej.grpc.middleware.GrpcMiddlewareService;
import de.cotto.lndmanagej.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SuppressWarnings("unused")
class ApplicationContextIT {
    @Autowired
    private ChannelService channelService;

    @MockitoBean
    private GrpcService grpcService;

    @MockitoBean
    private GrpcMiddlewareService grpcMiddlewareService;

    @MockitoBean
    private GrpcRouterService grpcRouterService;

    @MockitoBean
    private GrpcInvoicesService grpcInvoicesService;

    @Test
    void contextStarts() {
        assertThat(channelService).isNotNull();
    }
}
