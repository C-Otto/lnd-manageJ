package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

class RequestResponseListenerTest {
    private static final String REQUEST_TYPE = "request-type";
    private static final String RESPONSE_TYPE = "response-type";
    private static final String CRASH_KEYWORD = "crash";
    private final TestableRequestResponseListener messageListener = new TestableRequestResponseListener();

    @Test
    void getRequestType() {
        assertThat(messageListener.getRequestType()).isEqualTo(REQUEST_TYPE);
    }

    @Test
    void getResponseType() {
        assertThat(messageListener.getResponseType()).isEqualTo(RESPONSE_TYPE);
    }

    @Test
    void parses_request() {
        messageListener.acceptRequest(ByteString.copyFromUtf8("bar"), 123);
        assertThat(messageListener.request).isEqualTo("bar");
        assertThat(messageListener.requestId).isEqualTo(123);
    }

    @Test
    void parses_response() {
        messageListener.acceptResponse(ByteString.copyFromUtf8("foo"), 456);
        assertThat(messageListener.response).isEqualTo("foo");
        assertThat(messageListener.requestId).isEqualTo(456);
    }

    @Test
    void parse_error_response() {
        messageListener.acceptResponse(ByteString.copyFromUtf8(CRASH_KEYWORD), 456);
        assertThat(messageListener.response).isNull();
        assertThat(messageListener.requestId).isZero();
    }

    private static class TestableRequestResponseListener extends RequestResponseListener<String, String> {
        @Nullable
        private String request;
        @Nullable
        private String response;
        private long requestId;

        public TestableRequestResponseListener() {
            super(
                    REQUEST_TYPE,
                    TestableRequestResponseListener::parser,
                    RESPONSE_TYPE,
                    TestableRequestResponseListener::parser
            );
        }

        private static String parser(ByteString bytes) throws InvalidProtocolBufferException {
            if (CRASH_KEYWORD.equals(bytes.toStringUtf8())) {
                throw new InvalidProtocolBufferException(CRASH_KEYWORD);
            }
            return bytes.toStringUtf8();
        }

        @Override
        public void acceptRequest(String request, long requestId) {
            this.request = request;
            this.requestId = requestId;
        }

        @Override
        public void acceptResponse(String response, long requestId) {
            this.response = response;
            this.requestId = requestId;
        }
    }
}
