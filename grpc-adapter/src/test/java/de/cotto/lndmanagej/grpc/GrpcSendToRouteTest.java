package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.HexString;
import lnrpc.Hop;
import lnrpc.MPPRecord;
import lnrpc.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import routerrpc.RouterOuterClass;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcSendToRouteTest {
    private static final int BLOCK_HEIGHT = 800_000;
    @InjectMocks
    private GrpcSendToRoute grpcSendToRoute;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Mock
    private GrpcRouterService grpcRouterService;

    @Test
    void block_height_not_available() {
        when(grpcGetInfo.getBlockHeight()).thenReturn(Optional.empty());
        grpcSendToRoute.sendToRoute(ROUTE, DECODED_PAYMENT_REQUEST);
        verifyNoInteractions(grpcRouterService);
    }

    @Test
    void sends_to_converted_route() {
        when(grpcGetInfo.getBlockHeight()).thenReturn(Optional.of(BLOCK_HEIGHT));
        grpcSendToRoute.sendToRoute(ROUTE, DECODED_PAYMENT_REQUEST);
        RouterOuterClass.SendToRouteRequest expectedRequest = RouterOuterClass.SendToRouteRequest.newBuilder()
                .setRoute(Route.newBuilder()
                        .setTotalTimeLock(ROUTE.getTotalTimeLock(BLOCK_HEIGHT, DECODED_PAYMENT_REQUEST.cltvExpiry()))
                        .addHops(Hop.newBuilder()
                                .setChanId(CHANNEL_ID.getShortChannelId())
                                .setExpiry(800_184)
                                .setAmtToForwardMsat(100_020)
                                .setFeeMsat(20)
                                .setPubKey(PUBKEY_2.toString())
                                .build())
                        .addHops(Hop.newBuilder()
                                .setChanId(CHANNEL_ID_3.getShortChannelId())
                                .setExpiry(800_144)
                                .setAmtToForwardMsat(100_000)
                                .setFeeMsat(20)
                                .setPubKey(PUBKEY_3.toString())
                                .build())
                        .addHops(Hop.newBuilder()
                                .setChanId(CHANNEL_ID_5.getShortChannelId())
                                .setExpiry(800_144)
                                .setAmtToForwardMsat(100_000)
                                .setPubKey(PUBKEY_4.toString())
                                .setMppRecord(MPPRecord.newBuilder()
                                        .setTotalAmtMsat(123_456)
                                        .setPaymentAddr(toByteString(DECODED_PAYMENT_REQUEST.paymentAddress()))
                                        .build())
                                .build())
                        .setTotalFeesMsat(40)
                        .setTotalAmtMsat(100_040)
                        .build())
                .setPaymentHash(toByteString(DECODED_PAYMENT_REQUEST.paymentHash()))
                .build();
        verify(grpcRouterService).sendToRoute(eq(expectedRequest), any());
    }

    private ByteString toByteString(HexString hexString) {
        return ByteString.copyFrom(hexString.getByteArray());
    }
}