package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
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
import routerrpc.RouterOuterClass.SendToRouteRequest;
import routerrpc.RouterOuterClass.SendToRouteResponse;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SendToRouteListenerTest {
    private static final int REQUEST_ID = 1;
    private static final HexString PREIMAGE = new HexString("FF00AB");

    @Mock
    private PaymentListener paymentListener;

    private SendToRouteListener sendToRouteListener;

    @BeforeEach
    void setUp() {
        sendToRouteListener = new SendToRouteListener(List.of(paymentListener));
    }

    @Test
    void getRequestType() {
        assertThat(sendToRouteListener.getRequestType()).isEqualTo("routerrpc.SendToRouteRequest");
    }

    @Test
    void getResponseType() {
        assertThat(sendToRouteListener.getResponseType()).isEqualTo("routerrpc.SendToRouteResponse");
    }

    @Test
    void responseWithoutRequest() {
        sendToRouteListener.acceptResponse(SendToRouteResponse.newBuilder().build(), REQUEST_ID);
        verifyNoInteractions(paymentListener);
    }

    @Test
    void notifiesListenerForSuccess() {
        List<PaymentAttemptHop> paymentAttemptHops = acceptRequestWithRoute();
        sendToRouteListener.acceptResponse(response(PREIMAGE), REQUEST_ID);

        verify(paymentListener).success(PREIMAGE, paymentAttemptHops);
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void hop_without_pubkey() {
        sendToRouteListener.acceptRequest(SendToRouteRequest.newBuilder()
                .setRoute(Route.newBuilder()
                        .addHops(hop(CHANNEL_ID_2, 456_000))
                        .build())
                .build(), REQUEST_ID);
        sendToRouteListener.acceptResponse(response(PREIMAGE), REQUEST_ID);
        List<PaymentAttemptHop> paymentAttemptHops = List.of(
                new PaymentAttemptHop(Optional.of(CHANNEL_ID_2), Coins.ofSatoshis(456), Optional.empty())
        );
        verify(paymentListener).success(PREIMAGE, paymentAttemptHops);
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void hop_without_channel_id_and_pubkey() {
        sendToRouteListener.acceptRequest(SendToRouteRequest.newBuilder()
                .setRoute(Route.newBuilder()
                        .addHops(hop(100))
                        .build())
                .build(), REQUEST_ID);
        sendToRouteListener.acceptResponse(response(PREIMAGE), REQUEST_ID);
        List<PaymentAttemptHop> paymentAttemptHops = List.of(
                new PaymentAttemptHop(Optional.empty(), Coins.ofMilliSatoshis(100), Optional.empty())
        );
        verify(paymentListener).success(PREIMAGE, paymentAttemptHops);
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void hop_without_channel_id() {
        HexString preimage = new HexString("FF00AB");
        sendToRouteListener.acceptRequest(SendToRouteRequest.newBuilder()
                .setRoute(Route.newBuilder()
                        .addHops(hop(123_000, PUBKEY))
                        .build())
                .build(), REQUEST_ID);
        sendToRouteListener.acceptResponse(response(preimage), REQUEST_ID);
        List<PaymentAttemptHop> paymentAttemptHops = List.of(
                new PaymentAttemptHop(Optional.empty(), Coins.ofSatoshis(123), Optional.of(PUBKEY))
        );
        verify(paymentListener).success(preimage, paymentAttemptHops);
        verifyNoMoreInteractions(paymentListener);
    }

    @Test
    void notifiesListenerForFailure() {
        List<PaymentAttemptHop> paymentAttemptHops = acceptRequestWithRoute();
        sendToRouteListener.acceptResponse(SendToRouteResponse.newBuilder()
                        .setFailure(Failure.newBuilder()
                                .setCode(Failure.FailureCode.TEMPORARY_CHANNEL_FAILURE)
                                .setFailureSourceIndex(3)
                                .build())
                .build(), REQUEST_ID);
        verify(paymentListener).failure(paymentAttemptHops, 15, 3);
        verifyNoMoreInteractions(paymentListener);
    }

    private SendToRouteResponse response(HexString preimage) {
        return SendToRouteResponse.newBuilder().setPreimage(ByteString.copyFrom(preimage.getByteArray())).build();
    }

    private List<PaymentAttemptHop> acceptRequestWithRoute() {
        sendToRouteListener.acceptRequest(SendToRouteRequest.newBuilder()
                .setRoute(Route.newBuilder()
                        .addHops(hop(CHANNEL_ID, 123_000, PUBKEY))
                        .addHops(hop(CHANNEL_ID_2, 456_000, PUBKEY_2))
                        .build())
                .build(), REQUEST_ID);
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
