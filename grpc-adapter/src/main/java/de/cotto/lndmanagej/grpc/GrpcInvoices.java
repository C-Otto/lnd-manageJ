package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.SettledInvoice;
import lnrpc.AddInvoiceResponse;
import lnrpc.Invoice;
import lnrpc.InvoiceHTLC;
import lnrpc.InvoiceHTLCState;
import lnrpc.ListInvoiceResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toMap;

@Component
public class GrpcInvoices {
    private static final int LIMIT = 1_000;
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final long KEYSEND_PREIMAGE = 5_482_373_484L;
    private static final long KEYSEND_DATA = 7_629_168L;
    private static final long KEYSEND_DATA_V2 = 34_349_334L;

    private final GrpcService grpcService;
    private final GrpcPayments grpcPayments;
    private final GrpcInvoicesService grpcInvoicesService;

    public GrpcInvoices(GrpcService grpcService, GrpcPayments grpcPayments, GrpcInvoicesService grpcInvoicesService) {
        this.grpcService = grpcService;
        this.grpcPayments = grpcPayments;
        this.grpcInvoicesService = grpcInvoicesService;
    }

    public int getLimit() {
        return LIMIT;
    }

    public Optional<List<SettledInvoice>> getSettledInvoicesAfter(long offset) {
        ListInvoiceResponse list = grpcService.getInvoices(offset, LIMIT).orElse(null);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.getInvoicesList().stream()
                .map(this::toSettledInvoice)
                .toList());
    }

    public Stream<SettledInvoice> getNewSettledInvoicesAfter(long settleIndexOffset) {
        return grpcService.subscribeToSettledInvoices(settleIndexOffset)
                .map(this::toStream)
                .orElse(Stream.of())
                .map(this::toSettledInvoice)
                .filter(SettledInvoice::isValid);
    }

    public Optional<DecodedPaymentRequest> createPaymentRequest(Coins amount, String description, Duration expiry) {
        Invoice invoiceRequest = Invoice.newBuilder()
                .setMemo(description)
                .setValueMsat(amount.milliSatoshis())
                .setExpiry(expiry.get(ChronoUnit.SECONDS))
                .build();
        return grpcService.addInvoice(invoiceRequest)
                .map(AddInvoiceResponse::getPaymentRequest)
                .map(grpcPayments::decodePaymentRequest)
                .map(Optional::orElseThrow);
    }

    public void cancelPaymentRequest(DecodedPaymentRequest decodedPaymentRequest) {
        grpcInvoicesService.cancelInvoice(decodedPaymentRequest.paymentHash());
    }

    private SettledInvoice toSettledInvoice(Invoice lndInvoice) {
        if (lndInvoice.getState() != Invoice.InvoiceState.SETTLED) {
            return SettledInvoice.INVALID;
        }
        return new SettledInvoice(
                lndInvoice.getAddIndex(),
                lndInvoice.getSettleIndex(),
                LocalDateTime.ofEpochSecond(lndInvoice.getSettleDate(), 0, UTC).atZone(UTC),
                HEX_FORMAT.formatHex(lndInvoice.getRHash().toByteArray()),
                Coins.ofMilliSatoshis(lndInvoice.getAmtPaidMsat()),
                lndInvoice.getMemo(),
                getKeysendMessage(lndInvoice),
                getReceivedVia(lndInvoice)
        );
    }

    private Optional<String> getKeysendMessage(Invoice lndInvoice) {
        return lndInvoice.getHtlcsList().stream()
                .map(InvoiceHTLC::getCustomRecordsMap)
                .filter(map -> map.containsKey(KEYSEND_PREIMAGE))
                .filter(map -> map.containsKey(KEYSEND_DATA) || map.containsKey(KEYSEND_DATA_V2))
                .map(map -> {
                    if (map.containsKey(KEYSEND_DATA)) {
                        return map.get(KEYSEND_DATA);
                    }
                    return map.get(KEYSEND_DATA_V2);
                })
                .map(ByteString::toStringUtf8)
                .findFirst();
    }

    private Map<ChannelId, Coins> getReceivedVia(Invoice lndInvoice) {
        return lndInvoice.getHtlcsList().stream()
                .filter(invoiceHTLC -> invoiceHTLC.getState().equals(InvoiceHTLCState.SETTLED))
                .collect(toMap(
                        invoiceHTLC -> ChannelId.fromShortChannelId(invoiceHTLC.getChanId()),
                        invoiceHTLC -> Coins.ofMilliSatoshis(invoiceHTLC.getAmtMsat()),
                        Coins::add
                ));
    }

    private Stream<Invoice> toStream(Iterator<Invoice> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }
}
