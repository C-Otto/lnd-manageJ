package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Route;
import lnrpc.Hop;
import lnrpc.MPPRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import routerrpc.RouterOuterClass.SendToRouteRequest;

import java.util.ArrayList;
import java.util.List;

@Component
public class GrpcSendToRoute {
    private final GrpcRouterService grpcRouterService;
    private final GrpcGetInfo grpcGetInfo;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public GrpcSendToRoute(GrpcRouterService grpcRouterService, GrpcGetInfo grpcGetInfo) {
        this.grpcRouterService = grpcRouterService;
        this.grpcGetInfo = grpcGetInfo;
    }

    @Async
    public void sendToRoute(Route route, DecodedPaymentRequest decodedPaymentRequest, SendToRouteObserver observer) {
        Integer blockHeight = grpcGetInfo.getBlockHeight().orElse(null);
        if (blockHeight == null) {
            logger.error("Unable to get current block height");
            return;
        }
        SendToRouteRequest request = buildRequest(
                decodedPaymentRequest.paymentHash(),
                buildLndRoute(route, blockHeight, decodedPaymentRequest)
        );
        grpcRouterService.sendToRoute(request, new ReportingStreamObserver(observer));
    }

    public void forceFailureForPayment(DecodedPaymentRequest decodedPaymentRequest) {
        Route route = createLongRoute(decodedPaymentRequest);
        SendToRouteRequest request = buildRequest(
                decodedPaymentRequest.paymentHash(),
                buildLndRoute(route, 0, decodedPaymentRequest)
        );
        grpcRouterService.sendToRoute(request, new ReportingStreamObserver(new NoopSendToRouteObserver()));
    }

    private static Route createLongRoute(DecodedPaymentRequest decodedPaymentRequest) {
        ChannelId channelId = ChannelId.fromShortChannelId(111);
        Pubkey pubkey = Pubkey.create("000000000000000000000000000000000000000000000000000000000000000000");
        EdgeWithLiquidityInformation dummyEdge = new EdgeWithLiquidityInformation(
                new Edge(channelId, pubkey, pubkey, Coins.NONE, Policy.UNKNOWN),
                Coins.NONE,
                Coins.NONE
        );

        List<EdgeWithLiquidityInformation> hops = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            hops.add(dummyEdge);
        }
        return new Route(hops, decodedPaymentRequest.amount());
    }

    private lnrpc.Route buildLndRoute(Route route, int blockHeight, DecodedPaymentRequest decodedPaymentRequest) {
        int totalTimeLock = route.getTotalTimeLock(blockHeight, decodedPaymentRequest.cltvExpiry());
        Coins totalAmount = route.getAmount().add(route.getFees());
        Coins fees = route.getFees();
        lnrpc.Route.Builder routeBuilder = lnrpc.Route.newBuilder()
                .setTotalAmtMsat(totalAmount.milliSatoshis())
                .setTotalFeesMsat(fees.milliSatoshis())
                .setTotalTimeLock(totalTimeLock)
                .addAllHops(buildHops(route, blockHeight, decodedPaymentRequest));
        return routeBuilder.build();
    }

    private List<Hop> buildHops(Route route, int blockHeight, DecodedPaymentRequest decodedPaymentRequest) {
        List<Hop> hops = new ArrayList<>();
        List<Edge> edges = route.getEdges();
        for (int hopIndex = 0; hopIndex < edges.size(); hopIndex++) {
            Edge edge = edges.get(hopIndex);
            Hop.Builder hopBuilder = Hop.newBuilder();
            hopBuilder.setChanId(edge.channelId().getShortChannelId());
            hopBuilder.setPubKey(edge.endNode().toString());
            hopBuilder.setAmtToForwardMsat(route.getForwardAmountForHop(hopIndex).milliSatoshis());
            hopBuilder.setExpiry(route.getExpiryForHop(hopIndex, blockHeight, decodedPaymentRequest.cltvExpiry()));
            hopBuilder.setFeeMsat(route.getFeeForHop(hopIndex).milliSatoshis());
            hops.add(hopBuilder.build());
        }
        addMppRecord(hops, decodedPaymentRequest);
        return hops;
    }

    private void addMppRecord(List<Hop> hops, DecodedPaymentRequest decodedPaymentRequest) {
        HexString paymentAddress = decodedPaymentRequest.paymentAddress();
        Hop lastHop = hops.remove(hops.size() - 1);
        Coins totalAmount = decodedPaymentRequest.amount();
        MPPRecord mppRecord = MPPRecord.newBuilder()
                .setTotalAmtMsat(totalAmount.milliSatoshis())
                .setPaymentAddr(ByteString.copyFrom(paymentAddress.getByteArray()))
                .build();
        Hop lastHopWithMppRecord = Hop.newBuilder(lastHop).setMppRecord(mppRecord).build();
        hops.add(lastHopWithMppRecord);
    }

    private SendToRouteRequest buildRequest(HexString paymentHash, lnrpc.Route lndRoute) {
        return SendToRouteRequest.newBuilder()
                .setRoute(lndRoute)
                .setPaymentHash(ByteString.copyFrom(paymentHash.getByteArray()))
                .setSkipTempErr(true)
                .build();
    }

}
