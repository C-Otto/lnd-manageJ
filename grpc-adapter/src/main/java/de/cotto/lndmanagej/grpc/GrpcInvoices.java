package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import lnrpc.Invoice;
import lnrpc.ListInvoiceResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Component
public class GrpcInvoices {
    private static final int LIMIT = 1_000;
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final GrpcService grpcService;

    public GrpcInvoices(GrpcService grpcService) {
        this.grpcService = grpcService;
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

    public int getLimit() {
        return LIMIT;
    }
}
