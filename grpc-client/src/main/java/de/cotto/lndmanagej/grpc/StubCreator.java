package de.cotto.lndmanagej.grpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import lnrpc.LightningGrpc;
import routerrpc.RouterGrpc;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;

public class StubCreator {
    private static final int FIFTY_MEGA_BYTE = 50 * 1024 * 1024;

    private final LightningGrpc.LightningBlockingStub stub;
    private final LightningGrpc.LightningStub nonBlockingStub;
    private final RouterGrpc.RouterBlockingStub routerStub;
    private final ManagedChannel channel;
    private final File macaroonFile;
    private final File certFile;
    private final int port;
    private final String host;

    public StubCreator(File macaroonFile, File certFile, int port, String host) throws IOException {
        this.macaroonFile = macaroonFile;
        this.certFile = certFile;
        this.port = port;
        this.host = host;
        channel = getChannel();
        stub = createLightningStub();
        nonBlockingStub = createNonBlockingLightningStub();
        routerStub = createRouterStub();
    }

    public LightningGrpc.LightningBlockingStub getLightningStub() {
        return stub;
    }

    public LightningGrpc.LightningStub getNonBlockingLightningStub() {
        return nonBlockingStub;
    }

    public RouterGrpc.RouterBlockingStub getRouterStub() {
        return routerStub;
    }

    public void shutdown() {
        channel.shutdown();
    }

    private ManagedChannel getChannel() throws SSLException {
        SslContext sslContext = GrpcSslContexts.forClient().trustManager(certFile).build();
        return NettyChannelBuilder.forAddress(host, port).sslContext(sslContext).build();
    }

    private LightningGrpc.LightningBlockingStub createLightningStub() throws IOException {
        return LightningGrpc
                .newBlockingStub(channel)
                .withMaxInboundMessageSize(FIFTY_MEGA_BYTE)
                .withCallCredentials(new MacaroonCallCredential(macaroonFile));
    }

    private LightningGrpc.LightningStub createNonBlockingLightningStub() throws IOException {
        return LightningGrpc
                .newStub(channel)
                .withMaxInboundMessageSize(FIFTY_MEGA_BYTE)
                .withCallCredentials(new MacaroonCallCredential(macaroonFile));
    }

    private RouterGrpc.RouterBlockingStub createRouterStub() throws IOException {
        return RouterGrpc
                .newBlockingStub(channel)
                .withMaxInboundMessageSize(FIFTY_MEGA_BYTE)
                .withCallCredentials(new MacaroonCallCredential(macaroonFile));
    }
}
