package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.Failure;
import lnrpc.Hop;
import lnrpc.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.SendToRouteResponse;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SendToRouteListenerTest {
    private static final int REQUEST_ID = 1;
    private static final HexString NO_PREIMAGE = new HexString("");
    private static final ByteString NO_PREIMAGE_BYTESTRING = ByteString.copyFrom(NO_PREIMAGE.getByteArray());
    private static final HexString PREIMAGE = new HexString("FF00AB");
    private static final ByteString PREIMAGE_BYTESTRING = ByteString.copyFrom(PREIMAGE.getByteArray());
    private static final Failure NO_FAILURE = Failure.getDefaultInstance();

    @Mock
    private PaymentListenerUpdater paymentListenerUpdater;

    @InjectMocks
    private SendToRouteListener sendToRouteListener;

    @Test
    void getRequestType() {
        assertThat(sendToRouteListener.getRequestType()).isEqualTo("routerrpc.SendToRouteRequest");
    }

    @Test
    void getResponseType() {
        assertThat(sendToRouteListener.getResponseType()).isEqualTo("routerrpc.SendToRouteResponse");
    }

    @Test
    void notifies_for_request_before_response_arrives() {
        Route route = getRoute();
        acceptRequestWithRoute(route);
        verify(paymentListenerUpdater).forNewPaymentAttempt(route);
    }

    @Test
    void responseWithoutRequest() {
        sendToRouteListener.acceptResponse(SendToRouteResponse.newBuilder().build(), REQUEST_ID);
        verifyNoInteractions(paymentListenerUpdater);
    }

    @Test
    void passes_success_to_payment_listener_updater() {
        Route route = getRoute();
        acceptRequestWithRoute(route);
        SendToRouteResponse response = SendToRouteResponse.newBuilder()
                .setPreimage(PREIMAGE_BYTESTRING)
                .build();
        sendToRouteListener.acceptResponse(response, REQUEST_ID);
        verify(paymentListenerUpdater).forResponse(PREIMAGE_BYTESTRING, route, NO_FAILURE);
    }

    @Test
    void notifiesListenerForFailure() {
        Route route = getRoute();
        Failure failure = Failure.newBuilder()
                .setCode(Failure.FailureCode.TEMPORARY_CHANNEL_FAILURE)
                .setFailureSourceIndex(3)
                .build();
        acceptRequestWithRoute(route);

        SendToRouteResponse response = SendToRouteResponse.newBuilder()
                .setFailure(failure)
                .build();
        sendToRouteListener.acceptResponse(response, REQUEST_ID);
        verify(paymentListenerUpdater).forResponse(NO_PREIMAGE_BYTESTRING, route, failure);
    }

    private void acceptRequestWithRoute(Route route) {
        RouterOuterClass.SendToRouteRequest request = RouterOuterClass.SendToRouteRequest.newBuilder()
                .setRoute(route)
                .build();
        sendToRouteListener.acceptRequest(request, REQUEST_ID);
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
