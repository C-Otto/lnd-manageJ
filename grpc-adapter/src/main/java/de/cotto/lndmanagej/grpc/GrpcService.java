package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import io.grpc.StatusRuntimeException;
import lnrpc.ChanInfoRequest;
import lnrpc.Channel;
import lnrpc.ChannelEdge;
import lnrpc.GetInfoResponse;
import lnrpc.LightningGrpc;
import lnrpc.ListChannelsRequest;
import lnrpc.NodeInfo;
import lnrpc.NodeInfoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import routerrpc.RouterGrpc;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.SubscribeHtlcEventsRequest;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class GrpcService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final StubCreator stubCreator;
    private final LightningGrpc.LightningBlockingStub lightningStub;
    private final RouterGrpc.RouterBlockingStub routerStub;

    public GrpcService(LndConfiguration lndConfiguration) throws IOException {
        stubCreator = new StubCreator(
                lndConfiguration.getMacaroonFile(),
                lndConfiguration.getCertFile(),
                lndConfiguration.getPort(),
                lndConfiguration.getHost()
        );
        lightningStub = stubCreator.getLightningStub();
        routerStub = stubCreator.getRouterStub();
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    Optional<GetInfoResponse> getInfo() {
        return get(() -> lightningStub.getInfo(lnrpc.GetInfoRequest.getDefaultInstance()));
    }

    Iterator<RouterOuterClass.HtlcEvent> getHtlcEvents() {
        return get(() -> routerStub.subscribeHtlcEvents(SubscribeHtlcEventsRequest.newBuilder().build()))
                .orElse(Collections.emptyIterator());
    }

    private <X> Optional<X> get(Supplier<X> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (StatusRuntimeException exception) {
            logger.warn("Exception while connecting to lnd: ", exception);
            return Optional.empty();
        }
    }

    public List<Channel> getChannels() {
        return get(() -> lightningStub.listChannels(ListChannelsRequest.getDefaultInstance()).getChannelsList())
                .orElse(List.of());
    }

    public Optional<NodeInfo> getNodeInfo(Pubkey pubkey) {
        return get(() -> lightningStub.getNodeInfo(NodeInfoRequest.newBuilder().setPubKey(pubkey.toString()).build()));
    }

    public Optional<ChannelEdge> getChannelEdge(ChannelId channelId) {
        ChanInfoRequest build = ChanInfoRequest.newBuilder().setChanId(channelId.shortChannelId()).build();
        return get(() -> lightningStub.getChanInfo(build));
    }
}
