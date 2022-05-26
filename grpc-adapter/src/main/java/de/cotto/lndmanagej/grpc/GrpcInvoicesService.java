package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.annotation.Timed;
import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.HexString;
import invoicesrpc.InvoicesGrpc;
import invoicesrpc.InvoicesOuterClass;
import invoicesrpc.InvoicesOuterClass.CancelInvoiceMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Optional;

@Component
public class GrpcInvoicesService extends GrpcBase {
    private final InvoicesGrpc.InvoicesBlockingStub stub;

    public GrpcInvoicesService(
            ConfigurationService configurationService,
            @Value("${user.home}") String homeDirectory
    ) throws IOException {
        super(configurationService, homeDirectory);
        stub = stubCreator.getInvoicesStub();
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    @Timed
    public Optional<InvoicesOuterClass.CancelInvoiceResp> cancelInvoice(HexString paymentHash) {
        ByteString paymentHashByteString = ByteString.copyFrom(paymentHash.getByteArray());
        CancelInvoiceMsg request = CancelInvoiceMsg.newBuilder().setPaymentHash(paymentHashByteString).build();
        return get(() -> stub.cancelInvoice(request));
    }
}
