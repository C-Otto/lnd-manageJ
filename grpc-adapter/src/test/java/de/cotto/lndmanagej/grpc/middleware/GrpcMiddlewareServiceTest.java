package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
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

import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcMiddlewareServiceTest {
    private static final String REQUEST_TYPE = "request-type";
    private static final String RESPONSE_TYPE = "response-type";

    @Mock
    private GrpcService grpcService;

    @Mock
    private StreamObserver<RPCMiddlewareResponse> streamResponseObserver;

    private StreamObserver<RPCMiddlewareRequest> streamRequestObserver;

    @Mock
    private RequestListener<?> requestListener;

    @Mock
    private ResponseListener<?> responseListener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(requestListener.getRequestType()).thenReturn(REQUEST_TYPE);
        when(responseListener.getResponseType()).thenReturn(RESPONSE_TYPE);
        ArgumentCaptor<StreamObserver<RPCMiddlewareRequest>> captor = ArgumentCaptor.forClass(StreamObserver.class);
        when(grpcService.registerMiddleware(captor.capture())).thenReturn(streamResponseObserver);
        new GrpcMiddlewareService(grpcService, Set.of(requestListener), Set.of(responseListener));
        streamRequestObserver = captor.getValue();
    }

    @Test
    void registers_as_readonly_middleware_with_name() {
        verify(streamResponseObserver).onNext(argThat(this::isRegistrationMessage));
    }

    @Test
    void acknowledges_message() {
        int messageId = 123;
        streamRequestObserver.onNext(RPCMiddlewareRequest.newBuilder().setMsgId(messageId).build());
        verify(streamResponseObserver).onNext(argThat(v -> v.getRefMsgId() == messageId));
    }

    @Test
    void notifiesRequestListener() {
        ByteString expectedPayload = ByteString.copyFromUtf8("payload");
        RPCMessage request = RPCMessage.newBuilder().setTypeName(REQUEST_TYPE).setSerialized(expectedPayload).build();
        RPCMiddlewareRequest message =
                RPCMiddlewareRequest.newBuilder().setMsgId(123).setRequestId(456).setRequest(request).build();
        streamRequestObserver.onNext(message);
        verify(requestListener).acceptRequest(expectedPayload, 456);
    }

    @Test
    void notifiesResponseListener() {
        ByteString expectedPayload = ByteString.copyFromUtf8("payload");
        RPCMessage response = RPCMessage.newBuilder().setTypeName(RESPONSE_TYPE).setSerialized(expectedPayload).build();
        RPCMiddlewareRequest message =
                RPCMiddlewareRequest.newBuilder().setMsgId(123).setRequestId(456).setResponse(response).build();
        streamRequestObserver.onNext(message);
        verify(responseListener).acceptResponse(expectedPayload, 456);
    }

    private boolean isRegistrationMessage(RPCMiddlewareResponse value) {
        return value.hasRegister()
                && value.getRegister().getReadOnlyMode()
                && "lnd-manageJ".equals(value.getRegister().getMiddlewareName());
    }
}
