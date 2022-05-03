package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.PaymentListener;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.Failure;
import lnrpc.Hop;
import lnrpc.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentListenerUpdaterTest {
    private static final HexString NO_PREIMAGE = new HexString("");
    private static final HexString PREIMAGE = new HexString("FF00AB");
    private static final ByteString NO_PREIMAGE_BYTESTRING = ByteString.copyFrom(NO_PREIMAGE.getByteArray());
    private static final ByteString PREIMAGE_BYTESTRING = ByteString.copyFrom(PREIMAGE.getByteArray());
    private static final Failure NO_FAILURE = Failure.getDefaultInstance();

    private PaymentListenerUpdater paymentListenerUpdater;

    @Mock
    private PaymentListener paymentListener;

    @BeforeEach
    void setUp() {
        paymentListenerUpdater = new PaymentListenerUpdater(List.of(paymentListener));
    }

    @Test
    void notifies_listener_for_new_payment_attempt() {
        paymentListenerUpdater.forNewPaymentAttempt(getRoute());
        verify(paymentListener).forNewPaymentAttempt(getPaymentHops());
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void notifies_listener_for_success() {
        paymentListenerUpdater.forResponse(PREIMAGE_BYTESTRING, getRoute(), NO_FAILURE);

        verify(paymentListener).success(PREIMAGE, getPaymentHops());
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void hop_without_pubkey() {
        Route route = Route.newBuilder().addHops(hop(CHANNEL_ID_2, 456_000)).build();
        paymentListenerUpdater.forResponse(PREIMAGE_BYTESTRING, route, NO_FAILURE);
        List<PaymentAttemptHop> paymentAttemptHops = List.of(
                new PaymentAttemptHop(Optional.of(CHANNEL_ID_2), Coins.ofSatoshis(456), Optional.empty())
        );
        verify(paymentListener).success(PREIMAGE, paymentAttemptHops);
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void hop_without_channel_id_and_pubkey() {
        Route route = Route.newBuilder().addHops(hop(100)).build();
        paymentListenerUpdater.forResponse(PREIMAGE_BYTESTRING, route, NO_FAILURE);
        List<PaymentAttemptHop> paymentAttemptHops = List.of(
                new PaymentAttemptHop(Optional.empty(), Coins.ofMilliSatoshis(100), Optional.empty())
        );
        verify(paymentListener).success(PREIMAGE, paymentAttemptHops);
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void hop_without_channel_id() {
        Route route = Route.newBuilder().addHops(hop(123_000, PUBKEY)).build();
        paymentListenerUpdater.forResponse(PREIMAGE_BYTESTRING, route, NO_FAILURE);
        List<PaymentAttemptHop> paymentAttemptHops = List.of(
                new PaymentAttemptHop(Optional.empty(), Coins.ofSatoshis(123), Optional.of(PUBKEY))
        );
        verify(paymentListener).success(PREIMAGE, paymentAttemptHops);
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void notifiesListenerForFailure() {
        Failure failure = Failure.newBuilder()
                .setCode(Failure.FailureCode.TEMPORARY_CHANNEL_FAILURE)
                .setFailureSourceIndex(3)
                .build();
        paymentListenerUpdater.forResponse(NO_PREIMAGE_BYTESTRING, getRoute(), failure);

        List<PaymentAttemptHop> paymentAttemptHops = getPaymentHops();
        verify(paymentListener).failure(paymentAttemptHops, FailureCode.TEMPORARY_CHANNEL_FAILURE, 3);
        verifyNoMoreInteractions(paymentListener);
    }

    private Route getRoute() {
        return Route.newBuilder()
                .addHops(hop(CHANNEL_ID, 123_000, PUBKEY))
                .addHops(hop(CHANNEL_ID_2, 456_000, PUBKEY_2))
                .build();
    }

    private List<PaymentAttemptHop> getPaymentHops() {
        return List.of(
                new PaymentAttemptHop(Optional.of(CHANNEL_ID), Coins.ofSatoshis(123), Optional.of(PUBKEY)),
                new PaymentAttemptHop(Optional.of(CHANNEL_ID_2), Coins.ofSatoshis(456), Optional.of(PUBKEY_2))
        );
    }

    private Hop hop(ChannelId channelId, int value, Pubkey pubkey) {
        return Hop.newBuilder()
                .setChanId(channelId.getShortChannelId())
                .setAmtToForwardMsat(value)
                .setPubKey(pubkey.toString())
                .build();
    }

    @SuppressWarnings("SameParameterValue")
    private Hop hop(ChannelId channelId, int value) {
        return Hop.newBuilder()
                .setChanId(channelId.getShortChannelId())
                .setAmtToForwardMsat(value)
                .build();
    }

    @SuppressWarnings("SameParameterValue")
    private Hop hop(int value) {
        return Hop.newBuilder()
                .setAmtToForwardMsat(value)
                .build();
    }

    @SuppressWarnings("SameParameterValue")
    private Hop hop(int value, Pubkey pubkey) {
        return Hop.newBuilder()
                .setAmtToForwardMsat(value)
                .setPubKey(pubkey.toString())
                .build();
    }
}
