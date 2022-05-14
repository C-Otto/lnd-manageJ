package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import lnrpc.Failure;
import lnrpc.HTLCAttempt;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static de.cotto.lndmanagej.model.FailureCode.FINAL_INCORRECT_HTLC_AMOUNT;
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

    @Test
    void onNext_forwards_failure_code() {
        HTLCAttempt htlcAttempt = HTLCAttempt.newBuilder()
                .setFailure(Failure.newBuilder().setCodeValue(4).build())
                .build();
        assertThatCode(() -> reportingStreamObserver.onNext(htlcAttempt)).doesNotThrowAnyException();
        assertThat(sendToRouteObserver.seenFailureCode).isEqualTo(FINAL_INCORRECT_HTLC_AMOUNT);
    }

    private static class TestableSendToRouteObserver implements SendToRouteObserver {
        @Nullable
        private Throwable seenThrowable;

        @Nullable
        private HexString seenPreimage;

        @Nullable
        private FailureCode seenFailureCode;

        @Override
        public void onError(Throwable throwable) {
            seenThrowable = throwable;
        }

        @Override
        public void onValue(HexString preimage, FailureCode failureCode) {
            seenPreimage = preimage;
            seenFailureCode = failureCode;
        }
    }
}
