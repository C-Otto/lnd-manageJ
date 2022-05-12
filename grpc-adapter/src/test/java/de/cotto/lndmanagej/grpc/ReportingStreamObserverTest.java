package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.HexString;
import lnrpc.HTLCAttempt;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReportingStreamObserverTest {

    private final TestableSendToRouteObserver sendToRouteObserver = new TestableSendToRouteObserver();
    private final ReportingStreamObserver reportingStreamObserver = new ReportingStreamObserver(sendToRouteObserver);

    @Test
    void onCompleted() {
        assertThatCode(reportingStreamObserver::onCompleted).doesNotThrowAnyException();
    }

    @Test
    void onError() {
        NullPointerException throwable = new NullPointerException();
        assertThatCode(() -> reportingStreamObserver.onError(throwable)).doesNotThrowAnyException();
        assertThat(sendToRouteObserver.seenThrowable).isSameAs(throwable);
    }

    @Test
    void onNext_forwards_preimage() {
        HexString preimage = new HexString("AA00");
        HTLCAttempt htlcAttempt = HTLCAttempt.newBuilder()
                .setPreimage(ByteString.copyFrom(preimage.getByteArray()))
                .build();
        assertThatCode(() -> reportingStreamObserver.onNext(htlcAttempt)).doesNotThrowAnyException();
        assertThat(sendToRouteObserver.seenPreimage).isEqualTo(preimage);
    }

    private static class TestableSendToRouteObserver implements SendToRouteObserver {
        @Nullable
        private Throwable seenThrowable;

        @Nullable
        private HexString seenPreimage;

        @Override
        public void onError(Throwable throwable) {
            seenThrowable = throwable;
        }

        @Override
        public void onValue(HexString preimage) {
            seenPreimage = preimage;
        }
    }
}
