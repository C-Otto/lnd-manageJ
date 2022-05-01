package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.Failure;
import lnrpc.HTLCAttempt;
import lnrpc.Hop;
import lnrpc.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class HtlcAttemptListenerTest {
    // CPD-OFF
    private static final int REQUEST_ID = 1;
    private static final HexString NO_PREIMAGE = new HexString("");
    private static final ByteString NO_PREIMAGE_BYTESTRING = ByteString.copyFrom(NO_PREIMAGE.getByteArray());
    private static final HexString PREIMAGE = new HexString("FF00AB");
    private static final ByteString PREIMAGE_BYTESTRING = ByteString.copyFrom(PREIMAGE.getByteArray());
    private static final Failure NO_FAILURE = Failure.getDefaultInstance();
    // CPD-ON

    @Mock
    private PaymentListenerUpdater paymentListenerUpdater;

    @InjectMocks
    private HtlcAttemptListener htlcAttemptListener;

    @Test
    void getResponseType() {
        assertThat(htlcAttemptListener.getResponseType()).isEqualTo("lnrpc.HTLCAttempt");
    }

    @Test
    void passes_success_to_payment_listener_updater() {
        Route route = getRoute();
        HTLCAttempt response = HTLCAttempt.newBuilder()
                .setPreimage(PREIMAGE_BYTESTRING)
                .setRoute(route)
                .build();
        htlcAttemptListener.acceptResponse(response, REQUEST_ID);
        verify(paymentListenerUpdater).update(PREIMAGE_BYTESTRING, route, NO_FAILURE);
        verifyNoMoreInteractions(paymentListenerUpdater);
    }

    @Test
    void notifies_listener_for_failure() {
        Route route = getRoute();
        Failure failure = Failure.newBuilder()
                .setCode(Failure.FailureCode.TEMPORARY_CHANNEL_FAILURE)
                .setFailureSourceIndex(3)
                .build();

        HTLCAttempt response = HTLCAttempt.newBuilder()
                .setFailure(failure)
                .setRoute(route)
                .build();
        htlcAttemptListener.acceptResponse(response, REQUEST_ID);
        verify(paymentListenerUpdater).update(NO_PREIMAGE_BYTESTRING, route, failure);
        verifyNoMoreInteractions(paymentListenerUpdater);
    }

    private Route getRoute() {
        return Route.newBuilder()
                .addHops(hop(CHANNEL_ID, 123_000, PUBKEY))
                .addHops(hop(CHANNEL_ID_2, 456_000, PUBKEY_2))
                .build();
    }

    // CPD-OFF
    private Hop hop(ChannelId channelId, int value, Pubkey pubkey) {
        return Hop.newBuilder()
                .setChanId(channelId.getShortChannelId())
                .setAmtToForwardMsat(value)
                .setPubKey(pubkey.toString())
                .build();
    }
    // CPD-ON
}
