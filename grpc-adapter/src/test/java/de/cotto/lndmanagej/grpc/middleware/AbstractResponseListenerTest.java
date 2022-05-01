package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractResponseListenerTest {
    private static final String RESPONSE_TYPE = "response-type";
    private static final String CRASH_KEYWORD = "crash";
    private final TestableAbstractResponseListener messageListener = new TestableAbstractResponseListener();

    @Test
    void getResponseType() {
        assertThat(messageListener.getResponseType()).isEqualTo(RESPONSE_TYPE);
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

    private static class TestableAbstractResponseListener extends AbstractResponseListener<String> {
        @Nullable
        private String response;
        private long requestId;

        public TestableAbstractResponseListener() {
            super(RESPONSE_TYPE, TestableAbstractResponseListener::parser);
        }

        private static String parser(ByteString bytes) throws InvalidProtocolBufferException {
            if (CRASH_KEYWORD.equals(bytes.toStringUtf8())) {
                throw new InvalidProtocolBufferException(CRASH_KEYWORD);
            }
            return bytes.toStringUtf8();
        }

        @Override
        public void acceptResponse(String response, long requestId) {
            this.response = response;
            this.requestId = requestId;
        }
    }
}
