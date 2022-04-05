package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import lnrpc.RPCMessage;
import lnrpc.RPCMiddlewareRequest;
import lnrpc.RPCMiddlewareResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RequestAndResponseStreamObserverTest {
    private static final String REQUEST_LISTENER_TYPE = "request-listener-type";
    private static final String RESPONSE_LISTENER_TYPE = "response-listener-type";

    @InjectMocks
    private RequestAndResponseStreamObserver observer;

    @Mock
    private io.grpc.stub.StreamObserver<RPCMiddlewareResponse> responseObserver;

    @Mock
    private RequestListener<String> requestListener;

    @Mock
    private ResponseListener<String> responseListener;

    @BeforeEach
    void setUp() {
        lenient().when(requestListener.getRequestType()).thenReturn(REQUEST_LISTENER_TYPE);
        lenient().when(responseListener.getResponseType()).thenReturn(RESPONSE_LISTENER_TYPE);
        observer.initialize(responseObserver);
    }

    @Test
    void onError() {
        assertThatCode(() -> observer.onError(new NullPointerException())).doesNotThrowAnyException();
    }

    @Test
    void onCompleted() {
        assertThatCode(() -> observer.onCompleted()).doesNotThrowAnyException();
    }

    @Nested
    class OnNext {
        @Test
        void noListener() {
            observer.onNext(createRequestMessage("foo", 456, "payload"));
            verifyNoInteractions(responseListener);
            verifyNoInteractions(requestListener);
        }

        @Test
        void forwards_request_with_id() {
            observer.addRequestListener(requestListener);
            long messageId = 123;
            String payload = "request-payload";
            observer.onNext(createRequestMessage(REQUEST_LISTENER_TYPE, messageId, payload));
            verify(requestListener).acceptRequest(ByteString.copyFromUtf8(payload), messageId);
        }

        @Test
        void ignores_listener_with_other_type_for_request() {
            observer.addRequestListener(requestListener);
            observer.onNext(createRequestMessage("not-listener-type", 123, "xxx"));
            verify(requestListener, never()).acceptRequest(any(ByteString.class), anyLong());
        }

        @Test
        void forwards_response_with_id() {
            observer.addResponseListener(responseListener);
            long messageId = 456;
            String payload = "response-payload";
            observer.onNext(createResponseMessage(RESPONSE_LISTENER_TYPE, messageId, payload));
            verify(responseListener).acceptResponse(ByteString.copyFromUtf8(payload), messageId);
        }

        @Test
        void ignores_listener_with_other_type_for_response() {
            observer.addResponseListener(responseListener);
            observer.onNext(createResponseMessage("not-listener-type", 456, "yyy"));
            verify(responseListener, never()).acceptResponse(any(ByteString.class), anyLong());
        }

        private RPCMiddlewareRequest createRequestMessage(String type, long messageId, String payload) {
            return RPCMiddlewareRequest.newBuilder()
                    .setRequest(createMessage(type, payload))
                    .setMsgId(123).setRequestId(messageId).build();
        }

        private RPCMiddlewareRequest createResponseMessage(String type, long messageId, String payload) {
            return RPCMiddlewareRequest.newBuilder()
                    .setResponse(createMessage(type, payload))
                    .setMsgId(123).setRequestId(messageId).build();
        }

        private RPCMessage createMessage(String type, String payload) {
            return RPCMessage.newBuilder()
                    .setTypeName(type)
                    .setSerialized(ByteString.copyFromUtf8(payload))
                    .build();
        }
    }
}
