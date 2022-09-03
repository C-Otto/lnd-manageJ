package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings;
import de.cotto.lndmanagej.grpc.GrpcService;
import io.grpc.stub.StreamObserver;
import lnrpc.RPCMessage;
import lnrpc.RPCMiddlewareRequest;
import lnrpc.RPCMiddlewareResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcMiddlewareServiceTest {
    private static final String REQUEST_TYPE = "request-type";
    private static final String RESPONSE_TYPE = "response-type";

    @Nullable
    private GrpcMiddlewareService grpcMiddlewareService;

    @Mock
    private GrpcService grpcService;

    @Mock
    private StreamObserver<RPCMiddlewareResponse> streamResponseObserver;

    @Mock
    private RequestListener<?> requestListener;

    @Mock
    private ResponseListener<?> responseListener;

    @Mock
    private ConfigurationService configurationService;

    private ArgumentCaptor<StreamObserver<RPCMiddlewareRequest>> captor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        lenient().when(requestListener.getRequestType()).thenReturn(REQUEST_TYPE);
        lenient().when(responseListener.getResponseType()).thenReturn(RESPONSE_TYPE);
        captor = ArgumentCaptor.forClass(StreamObserver.class);
        lenient().when(grpcService.registerMiddleware(captor.capture())).thenReturn(streamResponseObserver);
        when(configurationService.getBooleanValue(PickhardtPaymentsConfigurationSettings.ENABLED))
                .thenReturn(Optional.of(true));
    }

    @Test
    void registers_as_readonly_middleware_with_name() {
        createServiceAndGetStreamRequestObserver();
        verify(streamResponseObserver).onNext(argThat(this::isRegistrationMessage));
    }

    @Test
    void does_not_register_if_disabled_in_configuration() {
        when(configurationService.getBooleanValue(PickhardtPaymentsConfigurationSettings.ENABLED))
                .thenReturn(Optional.of(false));
        grpcMiddlewareService = new GrpcMiddlewareService(grpcService, Set.of(), Set.of(), configurationService);
        verify(grpcService, never()).registerMiddleware(any());
    }

    @Test
    void does_not_register_if_not_configured() {
        when(configurationService.getBooleanValue(PickhardtPaymentsConfigurationSettings.ENABLED))
                .thenReturn(Optional.empty());
        grpcMiddlewareService = new GrpcMiddlewareService(grpcService, Set.of(), Set.of(), configurationService);
        verify(grpcService, never()).registerMiddleware(any());
    }

    @Test
    void acknowledges_message() {
        StreamObserver<RPCMiddlewareRequest> requestObserver = createServiceAndGetStreamRequestObserver();
        int messageId = 123;
        requestObserver.onNext(RPCMiddlewareRequest.newBuilder().setMsgId(messageId).build());
        verify(streamResponseObserver).onNext(argThat(v -> v.getRefMsgId() == messageId));
    }

    @Test
    void notifiesRequestListener() {
        StreamObserver<RPCMiddlewareRequest> requestObserver = createServiceAndGetStreamRequestObserver();
        ByteString expectedPayload = ByteString.copyFromUtf8("payload");
        RPCMessage request = RPCMessage.newBuilder().setTypeName(REQUEST_TYPE).setSerialized(expectedPayload).build();
        RPCMiddlewareRequest message =
                RPCMiddlewareRequest.newBuilder().setMsgId(123).setRequestId(456).setRequest(request).build();
        requestObserver.onNext(message);
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> verify(requestListener).acceptRequest(expectedPayload, 456)
        );
    }

    @Test
    void notifiesResponseListener() {
        StreamObserver<RPCMiddlewareRequest> requestObserver = createServiceAndGetStreamRequestObserver();
        ByteString expectedPayload = ByteString.copyFromUtf8("payload");
        RPCMessage response = RPCMessage.newBuilder().setTypeName(RESPONSE_TYPE).setSerialized(expectedPayload).build();
        RPCMiddlewareRequest message =
                RPCMiddlewareRequest.newBuilder().setMsgId(123).setRequestId(456).setResponse(response).build();
        requestObserver.onNext(message);
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> verify(responseListener).acceptResponse(expectedPayload, 456)
        );
    }

    @Test
    void isConnected() {
        createServiceAndGetStreamRequestObserver();
        assertThat(Objects.requireNonNull(grpcMiddlewareService).isConnected()).isTrue();
    }

    private boolean isRegistrationMessage(RPCMiddlewareResponse value) {
        return value.hasRegister()
                && value.getRegister().getReadOnlyMode()
                && "lnd-manageJ".equals(value.getRegister().getMiddlewareName());
    }

    private StreamObserver<RPCMiddlewareRequest> createServiceAndGetStreamRequestObserver() {
        grpcMiddlewareService = new GrpcMiddlewareService(
                grpcService,
                Set.of(requestListener),
                Set.of(responseListener),
                configurationService
        );
        return captor.getValue();
    }
}
