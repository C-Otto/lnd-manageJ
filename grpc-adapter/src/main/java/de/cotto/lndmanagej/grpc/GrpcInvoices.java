package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import lnrpc.Invoice;
import lnrpc.ListInvoiceResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class GrpcInvoices {
    private static final int LIMIT = 1_000;
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final GrpcService grpcService;

    public GrpcInvoices(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public int getLimit() {
        return LIMIT;
    }

    public Optional<List<SettledInvoice>> getSettledInvoicesAfter(long offset) {
        ListInvoiceResponse list = grpcService.getInvoices(offset, LIMIT).orElse(null);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.getInvoicesList().stream()
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

    private SettledInvoice toSettledInvoice(Invoice lndInvoice) {
        if (lndInvoice.getState() != Invoice.InvoiceState.SETTLED) {
            return SettledInvoice.INVALID;
        }
        return new SettledInvoice(
                lndInvoice.getAddIndex(),
                lndInvoice.getSettleIndex(),
                LocalDateTime.ofEpochSecond(lndInvoice.getSettleDate(), 0, ZoneOffset.UTC),
                HEX_FORMAT.formatHex(lndInvoice.getRHash().toByteArray()),
                Coins.ofMilliSatoshis(lndInvoice.getAmtPaidMsat()),
                lndInvoice.getMemo()
        );
    }

    private Stream<Invoice> toStream(Iterator<Invoice> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }
}
